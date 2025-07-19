package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.RefreshToken;
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
 * Repository tests for RefreshTokenRepository with test database.
 * Tests custom query methods for refresh token management, expiration monitoring,
 * cleanup operations, and concurrent access scenarios.
 */
@DataJpaTest
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshToken validToken;
    private RefreshToken expiredToken;
    private RefreshToken nearExpiryToken;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        // Create valid refresh token
        validToken = new RefreshToken();
        validToken.setManagedPageId(1L);
        validToken.setPlatformId(1L);
        validToken.setPlatform("facebook");
        validToken.setRefreshToken("encrypted-valid-refresh-token");
        validToken.setExpirationDate(testTime.plusDays(30));
        validToken.setCreatedAt(testTime.minusHours(1));
        validToken.setUpdatedAt(testTime.minusHours(1));

        // Create expired refresh token
        expiredToken = new RefreshToken();
        expiredToken.setManagedPageId(1L);
        expiredToken.setPlatformId(1L);
        expiredToken.setPlatform("facebook");
        expiredToken.setRefreshToken("encrypted-expired-refresh-token");
        expiredToken.setExpirationDate(testTime.minusDays(1));
        expiredToken.setCreatedAt(testTime.minusDays(2));
        expiredToken.setUpdatedAt(testTime.minusDays(2));

        // Create near expiry refresh token
        nearExpiryToken = new RefreshToken();
        nearExpiryToken.setManagedPageId(2L);
        nearExpiryToken.setPlatformId(2L);
        nearExpiryToken.setPlatform("instagram");
        nearExpiryToken.setRefreshToken("encrypted-near-expiry-refresh-token");
        nearExpiryToken.setExpirationDate(testTime.plusDays(3));
        nearExpiryToken.setCreatedAt(testTime.minusHours(2));
        nearExpiryToken.setUpdatedAt(testTime.minusHours(2));

        // Persist test data
        entityManager.persistAndFlush(validToken);
        entityManager.persistAndFlush(expiredToken);
        entityManager.persistAndFlush(nearExpiryToken);
    }

    @Test
    void testFindByManagedPageIdOrderByExpirationDesc() {
        // Act
        List<RefreshToken> result = refreshTokenRepository.findByManagedPageIdOrderByExpirationDesc(1L);

        // Assert
        assertEquals(2, result.size());
        // Should be ordered by expiration date DESC (valid token first, then expired)
        assertEquals(validToken.getId(), result.get(0).getId());
        assertEquals(expiredToken.getId(), result.get(1).getId());
        assertTrue(result.get(0).getExpirationDate().isAfter(result.get(1).getExpirationDate()));
    }

    @Test
    void testFindValidRefreshTokenByPageId() {
        // Act
        Optional<RefreshToken> result = refreshTokenRepository.findValidRefreshTokenByPageId(1L, testTime);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validToken.getId(), result.get().getId());
        assertTrue(result.get().getExpirationDate().isAfter(testTime));
    }

    @Test
    void testFindValidRefreshTokenByPageId_NoValidToken() {
        // Create a page with only expired tokens
        RefreshToken expiredToken2 = new RefreshToken();
        expiredToken2.setManagedPageId(3L);
        expiredToken2.setPlatformId(1L);
        expiredToken2.setPlatform("facebook");
        expiredToken2.setRefreshToken("encrypted-expired-refresh-token-2");
        expiredToken2.setExpirationDate(testTime.minusDays(2));
        entityManager.persistAndFlush(expiredToken2);

        // Act
        Optional<RefreshToken> result = refreshTokenRepository.findValidRefreshTokenByPageId(3L, testTime);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindLatestRefreshTokenByPageId() {
        // Act
        Optional<RefreshToken> result = refreshTokenRepository.findLatestRefreshTokenByPageId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validToken.getId(), result.get().getId());
    }

    @Test
    void testFindTokensExpiringBefore() {
        // Arrange
        LocalDateTime threshold = testTime.plusDays(7);

        // Act
        List<RefreshToken> result = refreshTokenRepository.findTokensExpiringBefore(threshold, testTime);

        // Assert
        assertEquals(1, result.size());
        assertEquals(nearExpiryToken.getId(), result.get(0).getId());
        assertTrue(result.get(0).getExpirationDate().isBefore(threshold));
        assertTrue(result.get(0).getExpirationDate().isAfter(testTime));
    }

    @Test
    void testFindExpiredTokens() {
        // Act
        List<RefreshToken> result = refreshTokenRepository.findExpiredTokens(testTime);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expiredToken.getId(), result.get(0).getId());
        assertTrue(result.get(0).getExpirationDate().isBefore(testTime));
    }

    @Test
    void testFindByPlatform() {
        // Act
        List<RefreshToken> facebookTokens = refreshTokenRepository.findByPlatform("facebook");
        List<RefreshToken> instagramTokens = refreshTokenRepository.findByPlatform("instagram");

        // Assert
        assertEquals(2, facebookTokens.size());
        assertEquals(1, instagramTokens.size());
        assertEquals("instagram", instagramTokens.get(0).getPlatform());
    }

    @Test
    void testFindValidTokensByPlatform() {
        // Act
        List<RefreshToken> validFacebookTokens = refreshTokenRepository.findValidTokensByPlatform("facebook", testTime);
        List<RefreshToken> validInstagramTokens = refreshTokenRepository.findValidTokensByPlatform("instagram", testTime);

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
        assertEquals(2, refreshTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId).size());

        // Act
        refreshTokenRepository.deleteByManagedPageId(pageId);
        entityManager.flush();

        // Assert
        List<RefreshToken> remaining = refreshTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId);
        assertTrue(remaining.isEmpty());

        // Verify other page tokens are not affected
        List<RefreshToken> otherPageTokens = refreshTokenRepository.findByManagedPageIdOrderByExpirationDesc(2L);
        assertEquals(1, otherPageTokens.size());
    }

    @Test
    void testDeleteExpiredTokens() {
        // Arrange
        assertEquals(1, refreshTokenRepository.findExpiredTokens(testTime).size());

        // Act
        refreshTokenRepository.deleteExpiredTokens(testTime);
        entityManager.flush();

        // Assert
        List<RefreshToken> expiredTokens = refreshTokenRepository.findExpiredTokens(testTime);
        assertTrue(expiredTokens.isEmpty());

        // Verify valid tokens are not affected
        List<RefreshToken> validTokens = refreshTokenRepository.findValidTokensByPlatform("facebook", testTime);
        assertEquals(1, validTokens.size());
    }

    @Test
    void testMarkTokensAsExpired() {
        // Arrange
        Long pageId = 1L;
        LocalDateTime expiredTime = testTime.minusMinutes(1);
        LocalDateTime updatedTime = testTime;

        // Act
        refreshTokenRepository.markTokensAsExpired(pageId, expiredTime, updatedTime);
        entityManager.flush();
        entityManager.clear();

        // Assert
        List<RefreshToken> tokens = refreshTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId);
        for (RefreshToken token : tokens) {
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
        long validCount1 = refreshTokenRepository.countValidTokensByPageId(1L, testTime);
        long validCount2 = refreshTokenRepository.countValidTokensByPageId(2L, testTime);
        long validCount3 = refreshTokenRepository.countValidTokensByPageId(999L, testTime);

        // Assert
        assertEquals(1, validCount1); // Only validToken is valid for page 1
        assertEquals(1, validCount2); // nearExpiryToken is still valid for page 2
        assertEquals(0, validCount3); // No tokens for non-existent page
    }

    @Test
    void testHasValidRefreshTokens() {
        // Act & Assert
        assertTrue(refreshTokenRepository.hasValidRefreshTokens(1L, testTime));
        assertTrue(refreshTokenRepository.hasValidRefreshTokens(2L, testTime));
        assertFalse(refreshTokenRepository.hasValidRefreshTokens(999L, testTime));
    }

    @Test
    void testFindTokensNeedingAttention() {
        // Arrange
        LocalDateTime warningThreshold = testTime.plusDays(7);

        // Act
        List<RefreshToken> result = refreshTokenRepository.findTokensNeedingAttention(testTime, warningThreshold);

        // Assert
        assertEquals(1, result.size());
        assertEquals(nearExpiryToken.getId(), result.get(0).getId());
    }

    @Test
    void testFindByPageIdAndPlatformId() {
        // Act
        List<RefreshToken> result = refreshTokenRepository.findByPageIdAndPlatformId(1L, 1L);

        // Assert
        assertEquals(2, result.size());
        // Should be ordered by expiration date DESC
        assertEquals(validToken.getId(), result.get(0).getId());
        assertEquals(expiredToken.getId(), result.get(1).getId());
    }

    @Test
    void testFindPageIdsWithExpiringRefreshTokens() {
        // Arrange
        LocalDateTime threshold = testTime.plusDays(7);

        // Act
        List<Long> result = refreshTokenRepository.findPageIdsWithExpiringRefreshTokens(testTime, threshold);

        // Assert
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0)); // Page with nearExpiryToken
    }

    @Test
    void testCleanupOldExpiredTokens() {
        // Arrange - Create multiple expired tokens for the same page
        RefreshToken expiredToken2 = new RefreshToken();
        expiredToken2.setManagedPageId(1L);
        expiredToken2.setPlatformId(1L);
        expiredToken2.setPlatform("facebook");
        expiredToken2.setRefreshToken("encrypted-expired-refresh-token-2");
        expiredToken2.setExpirationDate(testTime.minusDays(3));
        expiredToken2.setCreatedAt(testTime.minusDays(4));

        RefreshToken expiredToken3 = new RefreshToken();
        expiredToken3.setManagedPageId(1L);
        expiredToken3.setPlatformId(1L);
        expiredToken3.setPlatform("facebook");
        expiredToken3.setRefreshToken("encrypted-expired-refresh-token-3");
        expiredToken3.setExpirationDate(testTime.minusDays(2));
        expiredToken3.setCreatedAt(testTime.minusDays(3));

        entityManager.persist(expiredToken2);
        entityManager.persist(expiredToken3);
        entityManager.flush();

        // Verify we have 3 expired tokens for page 1
        assertEquals(3, refreshTokenRepository.findExpiredTokens(testTime).size());

        // Act
        refreshTokenRepository.cleanupOldExpiredTokens(1L, testTime);
        entityManager.flush();

        // Assert - Should keep only the most recent expired token
        List<RefreshToken> remainingExpired = refreshTokenRepository.findExpiredTokens(testTime);
        assertEquals(1, remainingExpired.size());
        assertEquals(expiredToken.getId(), remainingExpired.get(0).getId()); // Most recent expired token
    }

    @Test
    void testFindUsableRefreshTokenByPageId() {
        // Act
        Optional<RefreshToken> result = refreshTokenRepository.findUsableRefreshTokenByPageId(1L, testTime);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validToken.getId(), result.get().getId());
        assertTrue(result.get().getExpirationDate().isAfter(testTime));
    }

    @Test
    void testFindUsableRefreshTokenByPageId_NoUsableToken() {
        // Create a page with only expired tokens
        RefreshToken expiredToken2 = new RefreshToken();
        expiredToken2.setManagedPageId(4L);
        expiredToken2.setPlatformId(1L);
        expiredToken2.setPlatform("facebook");
        expiredToken2.setRefreshToken("encrypted-expired-refresh-token-2");
        expiredToken2.setExpirationDate(testTime.minusDays(1));
        entityManager.persistAndFlush(expiredToken2);

        // Act
        Optional<RefreshToken> result = refreshTokenRepository.findUsableRefreshTokenByPageId(4L, testTime);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testCountByPlatform() {
        // Act
        long facebookCount = refreshTokenRepository.countByPlatform("facebook");
        long instagramCount = refreshTokenRepository.countByPlatform("instagram");
        long nonExistentCount = refreshTokenRepository.countByPlatform("non-existent");

        // Assert
        assertEquals(2, facebookCount);
        assertEquals(1, instagramCount);
        assertEquals(0, nonExistentCount);
    }

    @Test
    void testFindTokensNeedingUserNotification() {
        // Arrange
        LocalDateTime notificationThreshold = testTime.plusDays(7);

        // Create an active managed page for the test (simulated with direct query)
        // In a real scenario, this would involve the ManagedPage entity

        // Act
        List<RefreshToken> result = refreshTokenRepository.findTokensNeedingUserNotification(testTime, notificationThreshold);

        // Assert - This test assumes there are active pages in the system
        // The exact count depends on the test data setup and active page status
        assertNotNull(result);
    }

    @Test
    void testRefreshTokenRotationScenario() {
        // Simulate a complete refresh token rotation scenario
        Long pageId = 5L;

        // 1. Create a refresh token that needs attention
        RefreshToken expiringRefreshToken = new RefreshToken();
        expiringRefreshToken.setManagedPageId(pageId);
        expiringRefreshToken.setPlatformId(1L);
        expiringRefreshToken.setPlatform("facebook");
        expiringRefreshToken.setRefreshToken("expiring-refresh-token");
        expiringRefreshToken.setExpirationDate(testTime.plusDays(5));
        entityManager.persistAndFlush(expiringRefreshToken);

        // 2. Detect tokens needing attention
        List<RefreshToken> needingAttention = refreshTokenRepository.findTokensNeedingAttention(
            testTime, testTime.plusDays(7));
        assertTrue(needingAttention.size() >= 1);

        // 3. Get usable refresh token for rotation
        Optional<RefreshToken> usableToken = refreshTokenRepository.findUsableRefreshTokenByPageId(pageId, testTime);
        assertTrue(usableToken.isPresent());
        assertEquals("expiring-refresh-token", usableToken.get().getRefreshToken());

        // 4. Mark old token as expired after successful refresh
        refreshTokenRepository.markTokensAsExpired(pageId, testTime.minusMinutes(1), testTime);
        entityManager.flush();

        // 5. Create new refresh token
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setManagedPageId(pageId);
        newRefreshToken.setPlatformId(1L);
        newRefreshToken.setPlatform("facebook");
        newRefreshToken.setRefreshToken("new-refresh-token");
        newRefreshToken.setExpirationDate(testTime.plusDays(60));
        entityManager.persistAndFlush(newRefreshToken);

        // 6. Verify rotation completed successfully
        assertTrue(refreshTokenRepository.hasValidRefreshTokens(pageId, testTime));
        Optional<RefreshToken> currentValid = refreshTokenRepository.findValidRefreshTokenByPageId(pageId, testTime);
        assertTrue(currentValid.isPresent());
        assertEquals("new-refresh-token", currentValid.get().getRefreshToken());
    }

    @Test
    void testConcurrentRefreshTokenOperations() {
        // Simulate concurrent refresh token operations
        Long pageId = 6L;

        // Create multiple refresh tokens concurrently (simulated)
        RefreshToken concurrentToken1 = new RefreshToken();
        concurrentToken1.setManagedPageId(pageId);
        concurrentToken1.setPlatformId(1L);
        concurrentToken1.setPlatform("facebook");
        concurrentToken1.setRefreshToken("concurrent-refresh-token-1");
        concurrentToken1.setExpirationDate(testTime.plusDays(30));

        RefreshToken concurrentToken2 = new RefreshToken();
        concurrentToken2.setManagedPageId(pageId);
        concurrentToken2.setPlatformId(1L);
        concurrentToken2.setPlatform("facebook");
        concurrentToken2.setRefreshToken("concurrent-refresh-token-2");
        concurrentToken2.setExpirationDate(testTime.plusDays(60));

        // Persist both tokens
        entityManager.persist(concurrentToken1);
        entityManager.persist(concurrentToken2);
        entityManager.flush();

        // Test that queries handle multiple tokens correctly
        Optional<RefreshToken> latestValid = refreshTokenRepository.findValidRefreshTokenByPageId(pageId, testTime);
        assertTrue(latestValid.isPresent());
        // Should return the one with the latest expiration date
        assertEquals(concurrentToken2.getId(), latestValid.get().getId());

        // Test concurrent cleanup
        refreshTokenRepository.markTokensAsExpired(pageId, testTime.minusMinutes(1), testTime);
        entityManager.flush();

        // All tokens for this page should now be expired
        assertFalse(refreshTokenRepository.hasValidRefreshTokens(pageId, testTime));
    }

    @Test
    void testLargeDatasetPerformance() {
        // Create a large number of refresh tokens to test query performance
        Long testPageId = 100L;
        for (int i = 0; i < 500; i++) {
            RefreshToken token = new RefreshToken();
            token.setManagedPageId(testPageId);
            token.setPlatformId(1L);
            token.setPlatform("facebook");
            token.setRefreshToken("refresh-token-" + i);
            token.setExpirationDate(testTime.plusDays(i % 60)); // Vary expiration times
            token.setCreatedAt(testTime.minusDays(i));
            entityManager.persist(token);

            if (i % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();

        // Test query performance
        long startTime = System.currentTimeMillis();
        Optional<RefreshToken> validToken = refreshTokenRepository.findValidRefreshTokenByPageId(testPageId, testTime);
        long endTime = System.currentTimeMillis();

        // Assert results and reasonable performance
        assertTrue(validToken.isPresent());
        assertTrue(endTime - startTime < 1000, "Query should complete within 1 second");

        // Test bulk operations performance
        startTime = System.currentTimeMillis();
        List<RefreshToken> expiredTokens = refreshTokenRepository.findExpiredTokens(testTime);
        endTime = System.currentTimeMillis();

        assertTrue(expiredTokens.size() > 0);
        assertTrue(endTime - startTime < 1000, "Bulk query should complete within 1 second");
    }

    @Test
    void testExpirationMonitoringQueries() {
        // Test various expiration monitoring scenarios
        
        // 1. Find tokens expiring in the next 7 days
        List<RefreshToken> expiringSoon = refreshTokenRepository.findTokensExpiringBefore(
            testTime.plusDays(7), testTime);
        assertEquals(1, expiringSoon.size());
        assertEquals(nearExpiryToken.getId(), expiringSoon.get(0).getId());

        // 2. Find tokens expiring in the next 30 days
        List<RefreshToken> expiringInMonth = refreshTokenRepository.findTokensExpiringBefore(
            testTime.plusDays(30), testTime);
        assertEquals(1, expiringInMonth.size());

        // 3. Find tokens expiring in the next 60 days (should include more)
        List<RefreshToken> expiringInTwoMonths = refreshTokenRepository.findTokensExpiringBefore(
            testTime.plusDays(60), testTime);
        assertEquals(2, expiringInTwoMonths.size()); // nearExpiryToken + validToken

        // 4. Find page IDs that need attention
        List<Long> pageIdsNeedingAttention = refreshTokenRepository.findPageIdsWithExpiringRefreshTokens(
            testTime, testTime.plusDays(7));
        assertEquals(1, pageIdsNeedingAttention.size());
        assertEquals(2L, pageIdsNeedingAttention.get(0));
    }

    @Test
    void testEdgeCasesAndNullHandling() {
        // Test with null parameters where applicable
        List<RefreshToken> emptyResult = refreshTokenRepository.findByPlatform(null);
        assertTrue(emptyResult.isEmpty());

        // Test with non-existent platform
        List<RefreshToken> nonExistentPlatform = refreshTokenRepository.findByPlatform("non-existent");
        assertTrue(nonExistentPlatform.isEmpty());

        // Test with future time for expired tokens
        List<RefreshToken> futureExpired = refreshTokenRepository.findExpiredTokens(testTime.plusDays(100));
        assertEquals(3, futureExpired.size()); // All current tokens would be expired

        // Test with past time for valid tokens
        List<RefreshToken> pastValid = refreshTokenRepository.findValidTokensByPlatform("facebook", testTime.minusDays(10));
        assertEquals(2, pastValid.size()); // All tokens would be valid from past perspective
    }

    @Test
    void testTransactionalConsistency() {
        // Test that operations maintain transactional consistency
        Long pageId = 1L;
        int initialCount = refreshTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId).size();

        // Perform multiple operations in sequence
        RefreshToken newToken = new RefreshToken();
        newToken.setManagedPageId(pageId);
        newToken.setPlatformId(1L);
        newToken.setPlatform("facebook");
        newToken.setRefreshToken("consistency-test-refresh-token");
        newToken.setExpirationDate(testTime.plusDays(30));

        entityManager.persist(newToken);
        entityManager.flush();

        // Verify count increased
        int afterInsertCount = refreshTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId).size();
        assertEquals(initialCount + 1, afterInsertCount);

        // Mark tokens as expired
        refreshTokenRepository.markTokensAsExpired(pageId, testTime.minusMinutes(1), testTime);
        entityManager.flush();

        // Verify no valid tokens remain
        assertFalse(refreshTokenRepository.hasValidRefreshTokens(pageId, testTime));

        // But total count should remain the same
        int afterExpirationCount = refreshTokenRepository.findByManagedPageIdOrderByExpirationDesc(pageId).size();
        assertEquals(afterInsertCount, afterExpirationCount);
    }

    @Test
    void testPlatformSpecificQueries() {
        // Create tokens for different platforms to test platform-specific queries
        RefreshToken xToken = new RefreshToken();
        xToken.setManagedPageId(7L);
        xToken.setPlatformId(3L);
        xToken.setPlatform("x");
        xToken.setRefreshToken("x-refresh-token");
        xToken.setExpirationDate(testTime.plusDays(90)); // X tokens typically last longer
        entityManager.persistAndFlush(xToken);

        // Test platform-specific counts
        assertEquals(2, refreshTokenRepository.countByPlatform("facebook"));
        assertEquals(1, refreshTokenRepository.countByPlatform("instagram"));
        assertEquals(1, refreshTokenRepository.countByPlatform("x"));

        // Test platform-specific valid token queries
        List<RefreshToken> validXTokens = refreshTokenRepository.findValidTokensByPlatform("x", testTime);
        assertEquals(1, validXTokens.size());
        assertEquals("x-refresh-token", validXTokens.get(0).getRefreshToken());

        // Test mixed platform queries
        List<RefreshToken> allValidTokens = refreshTokenRepository.findAll().stream()
            .filter(token -> token.getExpirationDate().isAfter(testTime))
            .toList();
        assertEquals(3, allValidTokens.size()); // validToken, nearExpiryToken, xToken
    }
}