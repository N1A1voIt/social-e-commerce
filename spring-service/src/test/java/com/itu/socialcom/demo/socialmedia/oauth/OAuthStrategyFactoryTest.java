package com.itu.socialcom.demo.socialmedia.oauth;

import com.itu.socialcom.demo.socialmedia.dto.OAuthTokenResponse;
import com.itu.socialcom.demo.socialmedia.dto.SocialMediaPage;
import com.itu.socialcom.demo.socialmedia.exception.UnsupportedPlatformException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class OAuthStrategyFactoryTest {

    @Mock
    private OAuthStrategy mockFacebookStrategy;

    @Mock
    private OAuthStrategy mockInstagramStrategy;

    private OAuthStrategyFactory factory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(mockFacebookStrategy.getPlatformName()).thenReturn("facebook");
        when(mockInstagramStrategy.getPlatformName()).thenReturn("instagram");
        
        Map<String, OAuthStrategy> strategies = new HashMap<>();
        strategies.put("facebook", mockFacebookStrategy);
        strategies.put("instagram", mockInstagramStrategy);
        
        factory = new OAuthStrategyFactory(strategies);
    }

    @Test
    void testGetStrategy_ValidPlatform_ReturnsCorrectStrategy() {
        OAuthStrategy strategy = factory.getStrategy("facebook");
        assertNotNull(strategy);
        assertEquals("facebook", strategy.getPlatformName());
    }

    @Test
    void testGetStrategy_CaseInsensitive_ReturnsCorrectStrategy() {
        OAuthStrategy strategy = factory.getStrategy("FACEBOOK");
        assertNotNull(strategy);
        assertEquals("facebook", strategy.getPlatformName());
    }

    @Test
    void testGetStrategy_UnsupportedPlatform_ThrowsException() {
        assertThrows(UnsupportedPlatformException.class, () -> {
            factory.getStrategy("unsupported");
        });
    }

    @Test
    void testGetStrategy_NullPlatform_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getStrategy(null);
        });
    }

    @Test
    void testGetStrategy_EmptyPlatform_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getStrategy("");
        });
    }

    @Test
    void testIsSupported_ValidPlatform_ReturnsTrue() {
        assertTrue(factory.isSupported("facebook"));
        assertTrue(factory.isSupported("instagram"));
    }

    @Test
    void testIsSupported_UnsupportedPlatform_ReturnsFalse() {
        assertFalse(factory.isSupported("unsupported"));
    }

    @Test
    void testIsSupported_NullPlatform_ReturnsFalse() {
        assertFalse(factory.isSupported(null));
    }

    @Test
    void testGetSupportedPlatforms_ReturnsAllPlatforms() {
        var supportedPlatforms = factory.getSupportedPlatforms();
        assertEquals(2, supportedPlatforms.size());
        assertTrue(supportedPlatforms.contains("facebook"));
        assertTrue(supportedPlatforms.contains("instagram"));
    }
}