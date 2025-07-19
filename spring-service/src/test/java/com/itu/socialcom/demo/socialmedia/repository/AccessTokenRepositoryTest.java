package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.AccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for AccessTokenRepository with test database.
 * Tests custom query methods for token management, rotation, cleanup operations,
 * and concurrent access scenarios.
 */
@DataJpaTest
@ActiveProfiles("test")
class AccessTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    private AccessToken validToken;
    private AccessToken expiredToken;
    private AccessToken nearExpiryToken;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        // Create valid access token
        validToken = new AccessToken();
        validToken.setManagedPageId(1L);
        validToken.setPlatformId(1L);
        validToken.setPlatform("facebook");
        validToken.setAccessToken("encrypted-valid-token");
        validToken.setExpirationDate(testTime.plusHours(2));
        validToken.setCreatedAt(testTime.minusHours(1));
        validToken.setUpdatedAt(testTime.minusHours(1));

        // Create expired access token
        expiredToken = new AccessToken();
        expiredToken.setManagedPageId(1L);
        expiredToken.setPlatformId(1L);
        expiredToken.setPlatform("facebook");
        expiredToken.setAccessToken("encrypted-expired-token");
        expiredToken.setExpirationDate(testTime.minusHours(1));
        expiredToken.setCreatedAt(testTime.minusHours(2));
        expiredToken.setUpdatedAt(testTime.minusHours(2));

        // Create near expiry access token
        nearExpiryToken = new AccessToken();
        nearExpiryToken.setManagedPageId(2L);
        nearExpiryToken.setPlatformId(2L);
        nearExpiryToken.setPlatform("instagram");
        nearExpiryToken.setAccessToken("encrypted-near-expiry-token");
        nearExpiryToken.setExpirationDate(testTime.plusMinutes(30));
        nearExpiryToken.setCreatedAt(testTime.minusMinutes(30));
        nearExpiryToken.setUpdatedAt(testTime.minusMinutes(30));

        // Persist test data
        entityManager.persistAndFlush(validToken);
        entityManager.persistAndFlush(expiredToken);
        entityManager.persistAndFlush(nearExpiryToken);
    }

    @Test
    void testFindByManagedPageIdOrderByExpirationDesc() {
        // Act
        List<AccessToken> result = accessTokenRepository.findByManagedPageIdOrderByExpirationDesc(1L);

        // Assert
        assertEquals(2, result.size());
        // Should be ordered by expiration date DESC (valid token first, then expired)
        assertEquals(validToken.getId(), result.get(0).getId());
        assertEquals(expiredToken.getId(), result.get(1).getId());
        assertTrue(result.get(0).getExpirationDate().isAfter(result.get(1).getExpirationDate()));
    }

    @Test
    void testFindValidTokenByPageId() {
        // Act
        Optional<AccessToken> result = accessTokenRepository.findValidTokenByPageId(1L, testTime);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validToken.getId(), result.get().getId());
        assertTrue(result.get().getExpirationDate().isAfter(testTime));
    }

    @Test
    void testFindValidTokenByPageId_NoValidToken() {
        // Create a page with only expired tokens
        AccessToken expiredToken2 = new AccessToken();
        expiredToken2.setManagedPageId(3L);
        expiredToken2.setPlatformId(1L);
        expiredToken2.setPlatform("facebook");
        expiredToken2.setAccessToken("encrypted-expired-token-2");
        expiredToken2.setExpirationDate(testTime.minusHours(2));
        entityManager.persistAndFlush(expiredToken2);

        // Act
        Optional<AccessToken> result = accessTokenRepository.findValidTokenByPageId(3L, testTime);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindLatestTokenByPageId() {
        // Act
        Optional<AccessToken> result = accessTokenRepository.findLatestTokenByPageId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validToken.getId(), result.get().getId());
    }

    @Test
    void testFindTokensExpiringBefore() {
        // Arrange
        LocalDateTime threshold = testTime.plusHours(1);

        // Act
        List<AccessToken> result = accessTokenRepository.findTokensExpiringBefore(threshold, testTime);

        // Assert
        assertEquals(1, result.size());
        assertEquals(nearExpiryToken.getId(), result.get(0).getId());
        assertTrue(result.get(0).getExpirationDate().isBefore(threshold));
        assertTrue(result.get(0).getExpirationDate().isAfter(testTime));
    }

    @Test
    void testFindExpiredTokens() {
        // Act
        List<AccessToken> result = accessTokenRepository.findExpiredTokens(testTime);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expiredToken.getId(), result.get(0).getId());
        assertTrue(result.get(0).getExpirationDate().isBefore(testTime));
    }

    @Test
    void testFindByPlatform() {
        // Act
        List<AccessToken> facebookTokens = accessTokenRepository.findByPlatform("facebook");
        List<AccessToken> instagramTokens = accessTokenRepository.findByPlatform("instagram");

        // Assert
        assertEquals(2, facebookTokens.size());
        assertEquals(1, instagramTokens.size());
        assertEquals("instagram", instagramTokens.get(0).getPlatform());
    }

    @Test
    void testFindValidTokensByPlatform() {
        // Act
        List<AccessToken> validFacebookTokens = accessTokenRepository.findValidTokensByPlatform("facebook", testTime);
        List<AccessToken> validInstagramTokens = accessTokenRepository.findValidTokensByPlatform("instagram", testTime);

        // Assert
        assertEquals(1, validFacebookTokens.size());
        assertEquals(validToken.getId(), validFacebookTokens.get(0).getId());

        assertEquals(1, validInstagramTokens.size());
        assertEquals(nearExpiryToken.getId(), validInstagramTokens.get(0).getId());
    }

    @Test
    void testDeleteByManagedPageId() {
        // Arrange
        Long pageId = 1L;
        assertEquals(2, accessTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId).size());

        // Act
        accessTokenRepository.deleteByManagedPageId(pageId);
        entityManager.flush();

        // Assert
        List<AccessToken> remaining = accessTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId);
        assertTrue(remaining.isEmpty());

        // Verify other page tokens are not affected
        List<AccessToken> otherPageTokens = accessTokenRepository.findByManagedPageIdOrderByExpirationDesc(2L);
        assertEquals(1, otherPageTokens.size());
    }

    @Test
    void testDeleteExpiredTokens() {
        // Arrange
        assertEquals(1, accessTokenRepository.findExpiredTokens(testTime).size());

        // Act
        accessTokenRepository.deleteExpiredTokens(testTime);
        entityManager.flush();

        // Assert
        List<AccessToken> expiredTokens = accessTokenRepository.findExpiredTokens(testTime);
        assertTrue(expiredTokens.isEmpty());

        // Verify valid tokens are not affected
        List<AccessToken> validTokens = accessTokenRepository.findValidTokensByPlatform("facebook", testTime);
        assertEquals(1, validTokens.size());
    }

    @Test
    void testMarkTokensAsExpired() {
        // Arrange
        Long pageId = 1L;
        LocalDateTime expiredTime = testTime.minusMinutes(1);
        LocalDateTime updatedTime = testTime;

        // Act
        accessTokenRepository.markTokensAsExpired(pageId, expiredTime, updatedTime);
        entityManager.flush();
        entityManager.clear();

        // Assert
        List<AccessToken> tokens = accessTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId);
        for (AccessToken token : tokens) {
            if (token.getExpirationDate().isAfter(expiredTime)) {
                // Token that was valid should now be marked as expired
                assertEquals(expiredTime.withNano(0), token.getExpirationDate().withNano(0));
                assertEquals(updatedTime.withNano(0), token.getUpdatedAt().withNano(0));
            }
        }
    }

    @Test
    void testCountValidTokensByPageId() {
        // Act
        long validCount1 = accessTokenRepository.countValidTokensByPageId(1L, testTime);
        long validCount2 = accessTokenRepository.countValidTokensByPageId(2L, testTime);
        long validCount3 = accessTokenRepository.countValidTokensByPageId(999L, testTime);

        // Assert
        assertEquals(1, validCount1); // Only validToken is valid for page 1
        assertEquals(1, validCount2); // nearExpiryToken is still valid for page 2
        assertEquals(0, validCount3); // No tokens for non-existent page
    }

    @Test
    void testHasValidTokens() {
        // Act & Assert
        assertTrue(accessTokenRepository.hasValidTokens(1L, testTime));
        assertTrue(accessTokenRepository.hasValidTokens(2L, testTime));
        assertFalse(accessTokenRepository.hasValidTokens(999L, testTime));
    }

    @Test
    void testFindTokensNeedingRotation() {
        // Arrange
        LocalDateTime rotationThreshold = testTime.plusHours(1);

        // Act
        List<AccessToken> result = accessTokenRepository.findTokensNeedingRotation(testTime, rotationThreshold);

        // Assert
        assertEquals(1, result.size());
        assertEquals(nearExpiryToken.getId(), result.get(0).getId());
    }

    @Test
    void testFindByPageIdAndPlatformId() {
        // Act
        List<AccessToken> result = accessTokenRepository.findByPageIdAndPlatformId(1L, 1L);

        // Assert
        assertEquals(2, result.size());
        // Should be ordered by expiration date DESC
        assertEquals(validToken.getId(), result.get(0).getId());
        assertEquals(expiredToken.getId(), result.get(1).getId());
    }

    @Test
    void testCleanupOldExpiredTokens() {
        // Arrange - Create multiple expired tokens for the same page
        AccessToken expiredToken2 = new AccessToken();
        expiredToken2.setManagedPageId(1L);
        expiredToken2.setPlatformId(1L);
        expiredToken2.setPlatform("facebook");
        expiredToken2.setAccessToken("encrypted-expired-token-2");
        expiredToken2.setExpirationDate(testTime.minusHours(3));
        expiredToken2.setCreatedAt(testTime.minusHours(4));

        AccessToken expiredToken3 = new AccessToken();
        expiredToken3.setManagedPageId(1L);
        expiredToken3.setPlatformId(1L);
        expiredToken3.setPlatform("facebook");
        expiredToken3.setAccessToken("encrypted-expired-token-3");
        expiredToken3.setExpirationDate(testTime.minusHours(2));
        expiredToken3.setCreatedAt(testTime.minusHours(3));

        entityManager.persist(expiredToken2);
        entityManager.persist(expiredToken3);
        entityManager.flush();

        // Verify we have 3 expired tokens for page 1
        assertEquals(3, accessTokenRepository.findExpiredTokens(testTime).size());

        // Act
        accessTokenRepository.cleanupOldExpiredTokens(1L, testTime);
        entityManager.flush();

        // Assert - Should keep only the most recent expired token
        List<AccessToken> remainingExpired = accessTokenRepository.findExpiredTokens(testTime);
        assertEquals(1, remainingExpired.size());
        assertEquals(expiredToken.getId(), remainingExpired.get(0).getId()); // Most recent expired token
    }

    @Test
    void testConcurrentTokenOperations() {
        // Simulate concurrent token operations
        Long pageId = 1L;

        // Create multiple tokens concurrently (simulated)
        AccessToken concurrentToken1 = new AccessToken();
        concurrentToken1.setManagedPageId(pageId);
        concurrentToken1.setPlatformId(1L);
        concurrentToken1.setPlatform("facebook");
        concurrentToken1.setAccessToken("concurrent-token-1");
        concurrentToken1.setExpirationDate(testTime.plusHours(1));

        AccessToken concurrentToken2 = new AccessToken();
        concurrentToken2.setManagedPageId(pageId);
        concurrentToken2.setPlatformId(1L);
        concurrentToken2.setPlatform("facebook");
        concurrentToken2.setAccessToken("concurrent-token-2");
        concurrentToken2.setExpirationDate(testTime.plusHours(3));

        // Persist both tokens
        entityManager.persist(concurrentToken1);
        entityManager.persist(concurrentToken2);
        entityManager.flush();

        // Test that queries handle multiple tokens correctly
        Optional<AccessToken> latestValid = accessTokenRepository.findValidTokenByPageId(pageId, testTime);
        assertTrue(latestValid.isPresent());
        // Should return the one with the latest expiration date
        assertEquals(concurrentToken2.getId(), latestValid.get().getId());

        // Test concurrent cleanup
        accessTokenRepository.markTokensAsExpired(pageId, testTime.minusMinutes(1), testTime);
        entityManager.flush();

        // All tokens for this page should now be expired
        assertFalse(accessTokenRepository.hasValidTokens(pageId, testTime));
    }

    @Test
    void testTokenRotationScenario() {
        // Simulate a complete token rotation scenario
        Long pageId = 4L;

        // 1. Create an expiring token
        AccessToken expiringToken = new AccessToken();
        expiringToken.setManagedPageId(pageId);
        expiringToken.setPlatformId(1L);
        expiringToken.setPlatform("facebook");
        expiringToken.setAccessToken("expiring-token");
        expiringToken.setExpirationDate(testTime.plusMinutes(15));
        entityManager.persistAndFlush(expiringToken);

        // 2. Detect tokens needing rotation
        List<AccessToken> needingRotation = accessTokenRepository.findTokensNeedingRotation(
            testTime, testTime.plusMinutes(30));
        assertEquals(1, needingRotation.size());

        // 3. Mark old token as expired
        accessTokenRepository.markTokensAsExpired(pageId, testTime.minusMinutes(1), testTime);
        entityManager.flush();

        // 4. Create new token
        AccessToken newToken = new AccessToken();
        newToken.setManagedPageId(pageId);
        newToken.setPlatformId(1L);
        newToken.setPlatform("facebook");
        newToken.setAccessToken("new-rotated-token");
        newToken.setExpirationDate(testTime.plusHours(1));
        entityManager.persistAndFlush(newToken);

        // 5. Verify rotation completed successfully
        assertTrue(accessTokenRepository.hasValidTokens(pageId, testTime));
        Optional<AccessToken> currentValid = accessTokenRepository.findValidTokenByPageId(pageId, testTime);
        assertTrue(currentValid.isPresent());
        assertEquals("new-rotated-token", currentValid.get().getAccessToken());
    }

    @Test
    void testLargeDatasetPerformance() {
        // Create a large number of tokens to test query performance
        Long testPageId = 100L;
        for (int i = 0; i < 1000; i++) {
            AccessToken token = new AccessToken();
            token.setManagedPageId(testPageId);
            token.setPlatformId(1L);
            token.setPlatform("facebook");
            token.setAccessToken("token-" + i);
            token.setExpirationDate(testTime.plusHours(i % 24)); // Vary expiration times
            token.setCreatedAt(testTime.minusHours(i));
            entityManager.persist(token);

            if (i % 100 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();

        // Test query performance
        long startTime = System.currentTimeMillis();
        Optional<AccessToken> validToken = accessTokenRepository.findValidTokenByPageId(testPageId, testTime);
        long endTime = System.currentTimeMillis();

        // Assert results and reasonable performance
        assertTrue(validToken.isPresent());
        assertTrue(endTime - startTime < 1000, "Query should complete within 1 second");

        // Test bulk operations performance
        startTime = System.currentTimeMillis();
        List<AccessToken> expiredTokens = accessTokenRepository.findExpiredTokens(testTime);
        endTime = System.currentTimeMillis();

        assertTrue(expiredTokens.size() > 0);
        assertTrue(endTime - startTime < 1000, "Bulk query should complete within 1 second");
    }

    @Test
    void testEdgeCasesAndNullHandling() {
        // Test with null parameters where applicable
        List<AccessToken> emptyResult = accessTokenRepository.findByPlatform(null);
        assertTrue(emptyResult.isEmpty());

        // Test with non-existent platform
        List<AccessToken> nonExistentPlatform = accessTokenRepository.findByPlatform("non-existent");
        assertTrue(nonExistentPlatform.isEmpty());

        // Test with future time for expired tokens
        List<AccessToken> futureExpired = accessTokenRepository.findExpiredTokens(testTime.plusDays(1));
        assertEquals(3, futureExpired.size()); // All current tokens would be expired

        // Test with past time for valid tokens
        List<AccessToken> pastValid = accessTokenRepository.findValidTokensByPlatform("facebook", testTime.minusDays(1));
        assertEquals(2, pastValid.size()); // All tokens would be valid from past perspective
    }

    @Test
    void testTransactionalConsistency() {
        // Test that operations maintain transactional consistency
        Long pageId = 1L;
        int initialCount = accessTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId).size();

        // Perform multiple operations in sequence
        AccessToken newToken = new AccessToken();
        newToken.setManagedPageId(pageId);
        newToken.setPlatformId(1L);
        newToken.setPlatform("facebook");
        newToken.setAccessToken("consistency-test-token");
        newToken.setExpirationDate(testTime.plusHours(1));

        entityManager.persist(newToken);
        entityManager.flush();

        // Verify count increased
        int afterInsertCount = accessTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId).size();
        assertEquals(initialCount + 1, afterInsertCount);

        // Mark tokens as expired
        accessTokenRepository.markTokensAsExpired(pageId, testTime.minusMinutes(1), testTime);
        entityManager.flush();

        // Verify no valid tokens remain
        assertFalse(accessTokenRepository.hasValidTokens(pageId, testTime));

        // But total count should remain the same
        int afterExpirationCount = accessTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId).size();
        assertEquals(afterInsertCount, afterExpirationCount);
    }
}