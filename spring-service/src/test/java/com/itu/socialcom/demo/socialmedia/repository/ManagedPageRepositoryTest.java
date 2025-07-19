package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for ManagedPageRepository with test database.
 * Tests custom query methods, data integrity constraints, and concurrent access scenarios.
 */
@DataJpaTest
@ActiveProfiles("test")
class ManagedPageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ManagedPageRepository managedPageRepository;

    private ManagedPage testPage1;
    private ManagedPage testPage2;
    private ManagedPage testPage3;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        // Create test pages for seller 100
        testPage1 = new ManagedPage();
        testPage1.setSellerId(100L);
        testPage1.setPlatformId(1L);
        testPage1.setPlatformIdentifier("facebook-page-1");
        testPage1.setPageTitle("Test Facebook Page 1");
        testPage1.setAssociatedMedia("https://example.com/image1.jpg");
        testPage1.setLinkToPlatform("https://facebook.com/page1");
        testPage1.setStatus("active");
        testPage1.setCreatedAt(testTime.minusHours(2));
        testPage1.setUpdatedAt(testTime.minusHours(1));

        testPage2 = new ManagedPage();
        testPage2.setSellerId(100L);
        testPage2.setPlatformId(2L);
        testPage2.setPlatformIdentifier("instagram-page-1");
        testPage2.setPageTitle("Test Instagram Page 1");
        testPage2.setAssociatedMedia("https://example.com/image2.jpg");
        testPage2.setLinkToPlatform("https://instagram.com/page1");
        testPage2.setStatus("inactive");
        testPage2.setCreatedAt(testTime.minusHours(1));
        testPage2.setUpdatedAt(testTime.minusMinutes(30));

        // Create test page for different seller
        testPage3 = new ManagedPage();
        testPage3.setSellerId(200L);
        testPage3.setPlatformId(1L);
        testPage3.setPlatformIdentifier("facebook-page-2");
        testPage3.setPageTitle("Test Facebook Page 2");
        testPage3.setAssociatedMedia("https://example.com/image3.jpg");
        testPage3.setLinkToPlatform("https://facebook.com/page2");
        testPage3.setStatus("active");
        testPage3.setCreatedAt(testTime.minusMinutes(30));
        testPage3.setUpdatedAt(testTime.minusMinutes(15));

        // Persist test data
        entityManager.persistAndFlush(testPage1);
        entityManager.persistAndFlush(testPage2);
        entityManager.persistAndFlush(testPage3);
    }

    @Test
    void testFindBySellerIdCustom() {
        // Act
        List<ManagedPage> result = managedPageRepository.findBySellerIdCustom(100L);

        // Assert
        assertEquals(2, result.size());
        // Should be ordered by createdAt DESC
        assertEquals(testPage2.getId(), result.get(0).getId()); // More recent
        assertEquals(testPage1.getId(), result.get(1).getId()); // Older
    }

    @Test
    void testFindBySellerIdCustom_NoResults() {
        // Act
        List<ManagedPage> result = managedPageRepository.findBySellerIdCustom(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindBySellerAndPlatform() {
        // Act
        List<ManagedPage> result = managedPageRepository.findBySellerAndPlatform(100L, 1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testPage1.getId(), result.get(0).getId());
        assertEquals("facebook-page-1", result.get(0).getPlatformIdentifier());
    }

    @Test
    void testFindBySellerAndPlatform_NoResults() {
        // Act
        List<ManagedPage> result = managedPageRepository.findBySellerAndPlatform(100L, 999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByPlatformIdentifierAndPlatform() {
        // Act
        Optional<ManagedPage> result = managedPageRepository.findByPlatformIdentifierAndPlatform("facebook-page-1", 1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPage1.getId(), result.get().getId());
        assertEquals(100L, result.get().getSellerId());
    }

    @Test
    void testFindByPlatformIdentifierAndPlatform_NotFound() {
        // Act
        Optional<ManagedPage> result = managedPageRepository.findByPlatformIdentifierAndPlatform("non-existent", 1L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindBySellerAndStatus() {
        // Act
        List<ManagedPage> activePages = managedPageRepository.findBySellerAndStatus(100L, "active");
        List<ManagedPage> inactivePages = managedPageRepository.findBySellerAndStatus(100L, "inactive");

        // Assert
        assertEquals(1, activePages.size());
        assertEquals(testPage1.getId(), activePages.get(0).getId());

        assertEquals(1, inactivePages.size());
        assertEquals(testPage2.getId(), inactivePages.get(0).getId());
    }

    @Test
    void testFindActivePagesBySeller() {
        // Act
        List<ManagedPage> result = managedPageRepository.findActivePagesBySeller(100L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testPage1.getId(), result.get(0).getId());
        assertEquals("active", result.get(0).getStatus());
    }

    @Test
    void testFindInactivePagesBySeller() {
        // Act
        List<ManagedPage> result = managedPageRepository.findInactivePagesBySeller(100L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testPage2.getId(), result.get(0).getId());
        assertEquals("inactive", result.get(0).getStatus());
    }

    @Test
    void testUpdatePageStatus() {
        // Arrange
        LocalDateTime updateTime = LocalDateTime.now();

        // Act
        managedPageRepository.updatePageStatus(testPage1.getId(), "inactive", updateTime);
        entityManager.flush();
        entityManager.clear();

        // Assert
        ManagedPage updated = managedPageRepository.findById(testPage1.getId()).orElseThrow();
        assertEquals("inactive", updated.getStatus());
        assertEquals(updateTime.withNano(0), updated.getUpdatedAt().withNano(0)); // Compare without nanoseconds
    }

    @Test
    void testUpdatePageStatusBatch() {
        // Arrange
        List<Long> pageIds = Arrays.asList(testPage1.getId(), testPage2.getId());
        LocalDateTime updateTime = LocalDateTime.now();

        // Act
        managedPageRepository.updatePageStatusBatch(pageIds, "requires_reauth", updateTime);
        entityManager.flush();
        entityManager.clear();

        // Assert
        ManagedPage updated1 = managedPageRepository.findById(testPage1.getId()).orElseThrow();
        ManagedPage updated2 = managedPageRepository.findById(testPage2.getId()).orElseThrow();

        assertEquals("requires_reauth", updated1.getStatus());
        assertEquals("requires_reauth", updated2.getStatus());
        assertEquals(updateTime.withNano(0), updated1.getUpdatedAt().withNano(0));
        assertEquals(updateTime.withNano(0), updated2.getUpdatedAt().withNano(0));
    }

    @Test
    void testExistsBySellerAndPlatformIdentifier() {
        // Act & Assert
        assertTrue(managedPageRepository.existsBySellerAndPlatformIdentifier(100L, "facebook-page-1", 1L));
        assertFalse(managedPageRepository.existsBySellerAndPlatformIdentifier(100L, "non-existent", 1L));
        assertFalse(managedPageRepository.existsBySellerAndPlatformIdentifier(999L, "facebook-page-1", 1L));
    }

    @Test
    void testCountBySellerAndStatus() {
        // Act
        long activeCount = managedPageRepository.countBySellerAndStatus(100L, "active");
        long inactiveCount = managedPageRepository.countBySellerAndStatus(100L, "inactive");
        long nonExistentCount = managedPageRepository.countBySellerAndStatus(100L, "non-existent");

        // Assert
        assertEquals(1, activeCount);
        assertEquals(1, inactiveCount);
        assertEquals(0, nonExistentCount);
    }

    @Test
    void testFindPagesNeedingTokenRefresh() {
        // Arrange
        LocalDateTime threshold = testTime.minusMinutes(45); // Between testPage1 and testPage2 update times

        // Act
        List<ManagedPage> result = managedPageRepository.findPagesNeedingTokenRefresh(threshold);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testPage1.getId(), result.get(0).getId());
    }

    @Test
    void testDataIntegrityConstraints() {
        // Test that we can't create duplicate platform identifiers for the same platform
        ManagedPage duplicatePage = new ManagedPage();
        duplicatePage.setSellerId(100L);
        duplicatePage.setPlatformId(1L);
        duplicatePage.setPlatformIdentifier("facebook-page-1"); // Same as testPage1
        duplicatePage.setPageTitle("Duplicate Page");
        duplicatePage.setStatus("active");

        // This should work as we don't have unique constraints in the entity
        // But in a real scenario, you might want to add unique constraints
        assertDoesNotThrow(() -> {
            entityManager.persistAndFlush(duplicatePage);
        });
    }

    @Test
    void testConcurrentAccess() {
        // Simulate concurrent updates to the same page
        Long pageId = testPage1.getId();
        LocalDateTime updateTime1 = LocalDateTime.now();
        LocalDateTime updateTime2 = updateTime1.plusSeconds(1);

        // First update
        managedPageRepository.updatePageStatus(pageId, "inactive", updateTime1);
        entityManager.flush();

        // Second update (should overwrite the first)
        managedPageRepository.updatePageStatus(pageId, "requires_reauth", updateTime2);
        entityManager.flush();
        entityManager.clear();

        // Assert final state
        ManagedPage updated = managedPageRepository.findById(pageId).orElseThrow();
        assertEquals("requires_reauth", updated.getStatus());
        assertEquals(updateTime2.withNano(0), updated.getUpdatedAt().withNano(0));
    }

    @Test
    void testQueryPerformanceWithLargeDataset() {
        // Create a larger dataset to test query performance
        for (int i = 0; i < 100; i++) {
            ManagedPage page = new ManagedPage();
            page.setSellerId(300L);
            page.setPlatformId((long) (i % 3 + 1)); // Platforms 1, 2, 3
            page.setPlatformIdentifier("page-" + i);
            page.setPageTitle("Page " + i);
            page.setStatus(i % 2 == 0 ? "active" : "inactive");
            page.setCreatedAt(testTime.minusHours(i));
            page.setUpdatedAt(testTime.minusMinutes(i));
            entityManager.persist(page);
        }
        entityManager.flush();

        // Test query performance
        long startTime = System.currentTimeMillis();
        List<ManagedPage> result = managedPageRepository.findBySellerIdCustom(300L);
        long endTime = System.currentTimeMillis();

        // Assert results and reasonable performance
        assertEquals(100, result.size());
        assertTrue(endTime - startTime < 1000, "Query should complete within 1 second");

        // Verify ordering (should be by createdAt DESC)
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getCreatedAt().isAfter(result.get(i + 1).getCreatedAt()) ||
                      result.get(i).getCreatedAt().equals(result.get(i + 1).getCreatedAt()));
        }
    }

    @Test
    void testComplexQueryScenarios() {
        // Create additional test data for complex scenarios
        ManagedPage complexPage1 = new ManagedPage();
        complexPage1.setSellerId(100L);
        complexPage1.setPlatformId(3L);
        complexPage1.setPlatformIdentifier("x-page-1");
        complexPage1.setPageTitle("Test X Page");
        complexPage1.setStatus("active");
        complexPage1.setCreatedAt(testTime);
        complexPage1.setUpdatedAt(testTime);

        ManagedPage complexPage2 = new ManagedPage();
        complexPage2.setSellerId(100L);
        complexPage2.setPlatformId(3L);
        complexPage2.setPlatformIdentifier("x-page-2");
        complexPage2.setPageTitle("Test X Page 2");
        complexPage2.setStatus("inactive");
        complexPage2.setCreatedAt(testTime.plusMinutes(1));
        complexPage2.setUpdatedAt(testTime.plusMinutes(1));

        entityManager.persist(complexPage1);
        entityManager.persist(complexPage2);
        entityManager.flush();

        // Test multiple platforms for same seller
        List<ManagedPage> platform3Pages = managedPageRepository.findBySellerAndPlatform(100L, 3L);
        assertEquals(2, platform3Pages.size());

        // Test status distribution
        long totalActive = managedPageRepository.countBySellerAndStatus(100L, "active");
        long totalInactive = managedPageRepository.countBySellerAndStatus(100L, "inactive");
        assertEquals(2, totalActive); // testPage1 + complexPage1
        assertEquals(2, totalInactive); // testPage2 + complexPage2

        // Test finding pages across all platforms
        List<ManagedPage> allPages = managedPageRepository.findBySellerIdCustom(100L);
        assertEquals(4, allPages.size());
    }

    @Test
    void testNullAndEdgeCaseHandling() {
        // Test with null/empty values where appropriate
        List<ManagedPage> emptyResult = managedPageRepository.findBySellerAndPlatform(null, 1L);
        assertTrue(emptyResult.isEmpty());

        // Test with very large seller ID
        List<ManagedPage> largeIdResult = managedPageRepository.findBySellerIdCustom(Long.MAX_VALUE);
        assertTrue(largeIdResult.isEmpty());

        // Test status counting with null
        long nullStatusCount = managedPageRepository.countBySellerAndStatus(100L, null);
        assertEquals(0, nullStatusCount);
    }

    @Test
    void testTransactionalBehavior() {
        // Test that updates are properly committed
        Long pageId = testPage1.getId();
        String originalStatus = testPage1.getStatus();
        LocalDateTime updateTime = LocalDateTime.now();

        // Update status
        managedPageRepository.updatePageStatus(pageId, "new_status", updateTime);
        
        // Flush to ensure the update is sent to database
        entityManager.flush();
        
        // Clear persistence context to force reload from database
        entityManager.clear();

        // Verify the change persisted
        ManagedPage reloaded = managedPageRepository.findById(pageId).orElseThrow();
        assertEquals("new_status", reloaded.getStatus());
        assertNotEquals(originalStatus, reloaded.getStatus());
    }
}