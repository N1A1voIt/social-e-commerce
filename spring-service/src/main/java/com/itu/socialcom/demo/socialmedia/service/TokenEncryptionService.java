package com.itu.socialcom.demo.socialmedia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting OAuth tokens before database storage.
 * Uses AES-GCM encryption for authenticated encryption with associated data (AEAD).
 */
@Service
public class TokenEncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenEncryptionService.class);
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int KEY_LENGTH = 256; // 256 bits
    
    private final SecretKey encryptionKey;
    private final SecureRandom secureRandom;
    
    @Autowired
    private OAuthAuditService auditService;
    
    public TokenEncryptionService(@Value("${oauth.encryption.key:}") String configuredKey) {
        this.secureRandom = new SecureRandom();
        
        if (configuredKey != null && !configuredKey.isEmpty()) {
            // Use configured key
            byte[] keyBytes = Base64.getDecoder().decode(configuredKey);
            this.encryptionKey = new SecretKeySpec(keyBytes, ALGORITHM);
            logger.info("Using configured encryption key for token encryption");
        } else {
            // Generate a new key (should be stored securely in production)
            this.encryptionKey = generateEncryptionKey();
            logger.warn("Generated new encryption key - this should be configured in production: {}", 
                       Base64.getEncoder().encodeToString(encryptionKey.getEncoded()));
        }
    }
    
    /**
     * Encrypt a token for secure database storage.
     * 
     * @param plainToken The plain text token to encrypt
     * @param platform The platform associated with the token (for audit logging)
     * @param tokenType The type of token (access/refresh) for audit logging
     * @return The encrypted token as a Base64-encoded string
     */
    public String encryptToken(String plainToken, String platform, String tokenType) {
        if (plainToken == null || plainToken.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Initialize cipher for encryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, gcmSpec);
            
            // Add platform and token type as associated data for additional security
            String associatedData = platform + ":" + tokenType;
            cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            
            // Encrypt the token
            byte[] encryptedToken = cipher.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedToken.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedToken, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedToken.length);
            
            String result = Base64.getEncoder().encodeToString(encryptedWithIv);
            
            logger.debug("Successfully encrypted {} token for platform {}", tokenType, platform);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to encrypt {} token for platform {}: {}", tokenType, platform, e.getMessage(), e);
            auditService.logSecurityEvent("TOKEN_ENCRYPTION_FAILED", platform, null, 
                                         "Failed to encrypt " + tokenType + " token: " + e.getMessage(), "system");
            throw new RuntimeException("Failed to encrypt token", e);
        }
    }
    
    /**
     * Decrypt a token from database storage.
     * 
     * @param encryptedToken The encrypted token as a Base64-encoded string
     * @param platform The platform associated with the token (for audit logging)
     * @param tokenType The type of token (access/refresh) for audit logging
     * @return The decrypted plain text token
     */
    public String decryptToken(String encryptedToken, String platform, String tokenType) {
        if (encryptedToken == null || encryptedToken.isEmpty()) {
            throw new IllegalArgumentException("Encrypted token cannot be null or empty");
        }
        
        try {
            // Decode from Base64
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedToken);
            
            if (encryptedWithIv.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted token format");
            }
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
            
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, gcmSpec);
            
            // Add platform and token type as associated data
            String associatedData = platform + ":" + tokenType;
            cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            
            // Decrypt the token
            byte[] decryptedToken = cipher.doFinal(encryptedData);
            
            String result = new String(decryptedToken, StandardCharsets.UTF_8);
            
            logger.debug("Successfully decrypted {} token for platform {}", tokenType, platform);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to decrypt {} token for platform {}: {}", tokenType, platform, e.getMessage(), e);
            auditService.logSecurityEvent("TOKEN_DECRYPTION_FAILED", platform, null, 
                                         "Failed to decrypt " + tokenType + " token: " + e.getMessage(), "system");
            throw new RuntimeException("Failed to decrypt token", e);
        }
    }
    
    /**
     * Generate a new encryption key.
     */
    private SecretKey generateEncryptionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
    
    /**
     * Rotate the encryption key (for key rotation scenarios).
     * This would require re-encrypting all existing tokens.
     */
    public String generateNewKeyForRotation() {
        SecretKey newKey = generateEncryptionKey();
        return Base64.getEncoder().encodeToString(newKey.getEncoded());
    }
    
    /**
     * Validate that a token can be decrypted (for health checks).
     */
    public boolean validateEncryption(String platform, String tokenType) {
        try {
            String testToken = "test_token_" + System.currentTimeMillis();
            String encrypted = encryptToken(testToken, platform, tokenType);
            String decrypted = decryptToken(encrypted, platform, tokenType);
            
            boolean isValid = testToken.equals(decrypted);
            
            if (!isValid) {
                auditService.logSecurityEvent("ENCRYPTION_VALIDATION_FAILED", platform, null, 
                                             "Token encryption validation failed", "system");
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Encryption validation failed for platform {}: {}", platform, e.getMessage(), e);
            return false;
        }
    }
}