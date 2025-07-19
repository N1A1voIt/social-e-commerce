package com.itu.socialcom.demo.socialmedia.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Test class for entity models to verify proper column mappings and functionality
 */
public class EntityModelsTest {

    private ManagedPage managedPage;
    private AccessToken accessToken;
    private RefreshToken refreshToken;
    private SupportedPlatform supportedPlatform;

    @BeforeEach
    void setUp() {
        managedPage = new ManagedPage();
        accessToken = new AccessToken();
        refreshToken = new RefreshToken();
        supportedPlatform = new SupportedPlatform();
    }

    @Test
    void testManagedPageEntity() {
        // Test basic properties
        managedPage.setPlatformIdentifier("12345");
        managedPage.setPageTitle("Test Page");
        managedPage.setAssociatedMedia("https://example.com/image.jpg");
        managedPage.setLinkToPlatform("https://facebook.com/testpage");
        managedPage.setPlatformId(1L);
        managedPage.setSellerId(100L);
        
        assertEquals("12345", managedPage.getPlatformIdentifier());
        assertEquals("Test Page", managedPage.getPageTitle());
        assertEquals("https://example.com/image.jpg", managedPage.getAssociatedMedia());
        assertEquals("https://facebook.com/testpage", managedPage.getLinkToPlatform());
        assertEquals(1L, managedPage.getPlatformId());
        assertEquals(100L, managedPage.getSellerId());
        
        // Test status methods
        managedPage.setActive();
        assertTrue(managedPage.isActive());
        assertEquals("active", managedPage.getStatus());
        
        managedPage.setInactive();
        assertFalse(managedPage.isActive());
        assertEquals("inactive", managedPage.getStatus());
    }

    @Test
    void testAccessTokenEntity() {
        LocalDateTime futureDate = LocalDateTime.now().plusHours(1);
        LocalDateTime pastDate = LocalDateTime.now().minusHours(1);
        
        // Test basic properties
        accessToken.setAccessToken("test_access_token");
        accessToken.setExpirationDate(futureDate);
        accessToken.setPlatform("facebook");
        accessToken.setPlatformId(1L);
        accessToken.setManagedPageId(10L);
        
        assertEquals("test_access_token", accessToken.getAccessToken());
        assertEquals(futureDate, accessToken.getExpirationDate());
        assertEquals("facebook", accessToken.getPlatform());
        assertEquals(1L, accessToken.getPlatformId());
        assertEquals(10L, accessToken.getManagedPageId());
        
        // Test expiration methods
        assertFalse(accessToken.isExpired());
        assertTrue(accessToken.isValid());
        
        accessToken.setExpirationDate(pastDate);
        assertTrue(accessToken.isExpired());
        assertFalse(accessToken.isValid());
        
        // Test expiring within methods
        accessToken.setExpirationDate(LocalDateTime.now().plusMinutes(30));
        assertTrue(accessToken.isExpiringWithin(60));
        assertFalse(accessToken.isExpiringWithin(15));
    }

    @Test
    void testRefreshTokenEntity() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(30);
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        
        // Test basic properties
        refreshToken.setRefreshToken("test_refresh_token");
        refreshToken.setExpirationDate(futureDate);
        refreshToken.setPlatform("facebook");
        refreshToken.setPlatformId(1L);
        refreshToken.setManagedPageId(10L);
        
        assertEquals("test_refresh_token", refreshToken.getRefreshToken());
        assertEquals(futureDate, refreshToken.getExpirationDate());
        assertEquals("facebook", refreshToken.getPlatform());
        assertEquals(1L, refreshToken.getPlatformId());
        assertEquals(10L, refreshToken.getManagedPageId());
        
        // Test expiration methods
        assertFalse(refreshToken.isExpired());
        assertTrue(refreshToken.isValid());
        
        refreshToken.setExpirationDate(pastDate);
        assertTrue(refreshToken.isExpired());
        assertFalse(refreshToken.isValid());
        
        // Test rotation methods
        refreshToken.setExpirationDate(LocalDateTime.now().plusDays(5));
        assertTrue(refreshToken.needsRotation(7));
        assertFalse(refreshToken.needsRotation(3));
    }

    @Test
    void testSupportedPlatformEntity() {
        // Test basic properties
        supportedPlatform.setLabel("facebook");
        assertEquals("facebook", supportedPlatform.getLabel());
        
        // Test platform type methods
        assertEquals(SupportedPlatform.PlatformType.FACEBOOK, supportedPlatform.getPlatformType());
        assertTrue(supportedPlatform.isPlatform(SupportedPlatform.PlatformType.FACEBOOK));
        assertFalse(supportedPlatform.isPlatform(SupportedPlatform.PlatformType.INSTAGRAM));
    }

    @Test
    void testPlatformTypeEnum() {
        // Test enum values
        assertEquals("facebook", SupportedPlatform.PlatformType.FACEBOOK.getLabel());
        assertEquals("instagram", SupportedPlatform.PlatformType.INSTAGRAM.getLabel());
        assertEquals("x", SupportedPlatform.PlatformType.X.getLabel());
        assertEquals("thread", SupportedPlatform.PlatformType.THREAD.getLabel());
        
        // Test fromLabel method
        assertEquals(SupportedPlatform.PlatformType.FACEBOOK, 
                    SupportedPlatform.PlatformType.fromLabel("facebook"));
        assertEquals(SupportedPlatform.PlatformType.INSTAGRAM, 
                    SupportedPlatform.PlatformType.fromLabel("INSTAGRAM"));
        
        // Test display names
        assertEquals("Facebook", SupportedPlatform.PlatformType.FACEBOOK.getDisplayName());
        assertEquals("Instagram", SupportedPlatform.PlatformType.INSTAGRAM.getDisplayName());
        assertEquals("X (Twitter)", SupportedPlatform.PlatformType.X.getDisplayName());
        assertEquals("Threads", SupportedPlatform.PlatformType.THREAD.getDisplayName());
        
        // Test OAuth support
        assertTrue(SupportedPlatform.PlatformType.FACEBOOK.supportsOAuth2());
        assertTrue(SupportedPlatform.PlatformType.INSTAGRAM.supportsOAuth2());
        assertTrue(SupportedPlatform.PlatformType.X.supportsOAuth2());
        assertFalse(SupportedPlatform.PlatformType.THREAD.supportsOAuth2());
        
        // Test invalid label
        assertThrows(IllegalArgumentException.class, () -> {
            SupportedPlatform.PlatformType.fromLabel("invalid");
        });
    }
}