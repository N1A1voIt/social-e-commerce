package com.itu.socialcom.demo.socialmedia.oauth;

import com.itu.socialcom.demo.socialmedia.exception.UnsupportedPlatformException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for creating OAuth strategy instances based on platform type.
 * Implements the Factory pattern to provide platform-specific OAuth implementations.
 */
@Component
public class OAuthStrategyFactory {
    
    private final Map<String, OAuthStrategy> strategies;
    
    public OAuthStrategyFactory(Map<String, OAuthStrategy> strategies) {
        this.strategies = strategies;
    }
    
    /**
     * Get OAuth strategy for the specified platform
     * @param platform Platform identifier (e.g., "facebook", "instagram", "x")
     * @return OAuth strategy implementation for the platform
     * @throws UnsupportedPlatformException if platform is not supported
     */
    public OAuthStrategy getStrategy(String platform) {
        if (platform == null || platform.trim().isEmpty()) {
            throw new IllegalArgumentException("Platform cannot be null or empty");
        }
        
        OAuthStrategy strategy = strategies.get(platform.toLowerCase());
        if (strategy == null) {
            throw new UnsupportedPlatformException("Platform not supported: " + platform);
        }
        return strategy;
    }
    
    /**
     * Check if a platform is supported
     * @param platform Platform identifier
     * @return true if platform is supported, false otherwise
     */
    public boolean isSupported(String platform) {
        return platform != null && strategies.containsKey(platform.toLowerCase());
    }
    
    /**
     * Get all supported platform names
     * @return Set of supported platform identifiers
     */
    public java.util.Set<String> getSupportedPlatforms() {
        return strategies.keySet();
    }
}