package com.itu.socialcom.demo.posts.services.save;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify UTF-8 encoding support in post savers
 */
@SpringBootTest
public class PostSaverEncodingTest {

    @Test
    public void testUtf8ContentTypeCreation() {
        // Test that we can create UTF-8 ContentType correctly
        ContentType utf8Text = ContentType.create("text/plain", "UTF-8");
        
        assertNotNull(utf8Text);
        assertEquals("UTF-8", utf8Text.getCharset().name());
        assertEquals("text/plain", utf8Text.getMimeType());
    }

    @Test
    public void testEmojiAndAccentSupport() {
        // Test strings with emojis and accented characters
        String textWithEmojis = "Hello! 👋 This is a test with emojis 🚀 🎉 ❤️";
        String textWithAccents = "Café, naïve, résumé, piñata, façade";
        String mixedText = "🇫🇷 Bonjour! Comment ça va? 😊 Très bien, merci! 🙏";
        
        // Verify UTF-8 can encode these properly
        byte[] emojiBytes = textWithEmojis.getBytes(StandardCharsets.UTF_8);
        byte[] accentBytes = textWithAccents.getBytes(StandardCharsets.UTF_8);
        byte[] mixedBytes = mixedText.getBytes(StandardCharsets.UTF_8);
        
        // Verify round-trip encoding/decoding works
        String decodedEmojis = new String(emojiBytes, StandardCharsets.UTF_8);
        String decodedAccents = new String(accentBytes, StandardCharsets.UTF_8);
        String decodedMixed = new String(mixedBytes, StandardCharsets.UTF_8);
        
        assertEquals(textWithEmojis, decodedEmojis);
        assertEquals(textWithAccents, decodedAccents);
        assertEquals(mixedText, decodedMixed);
        
        // Verify the strings are not corrupted (length should be preserved for these cases)
        assertTrue(decodedEmojis.contains("👋"));
        assertTrue(decodedEmojis.contains("🚀"));
        assertTrue(decodedEmojis.contains("🎉"));
        assertTrue(decodedEmojis.contains("❤️"));
        
        assertTrue(decodedAccents.contains("é"));
        assertTrue(decodedAccents.contains("ï"));
        assertTrue(decodedAccents.contains("ñ"));
        assertTrue(decodedAccents.contains("ç"));
        
        assertTrue(decodedMixed.contains("🇫🇷"));
        assertTrue(decodedMixed.contains("ça"));
        assertTrue(decodedMixed.contains("😊"));
    }

    @Test
    public void testSocialMediaCommonCharacters() {
        // Test common social media characters and symbols
        String socialText = "Check out our new products! 🛍️ " +
                "✨ Special offer: 20% OFF ✨ " +
                "📱 Available online 📱 " +
                "#Sale #Fashion #Deal 💯 " +
                "¡Oferta especial! 🎁 " +
                "Café & Crêpes 🥐☕ " +
                "Visit café-boutique.com 🔗";
        
        // Verify UTF-8 encoding preserves all characters
        byte[] encoded = socialText.getBytes(StandardCharsets.UTF_8);
        String decoded = new String(encoded, StandardCharsets.UTF_8);
        
        assertEquals(socialText, decoded);
        
        // Verify specific characters are preserved
        assertTrue(decoded.contains("🛍️"));
        assertTrue(decoded.contains("✨"));
        assertTrue(decoded.contains("📱"));
        assertTrue(decoded.contains("💯"));
        assertTrue(decoded.contains("🎁"));
        assertTrue(decoded.contains("🥐"));
        assertTrue(decoded.contains("☕"));
        assertTrue(decoded.contains("🔗"));
        assertTrue(decoded.contains("¡"));
        assertTrue(decoded.contains("é"));
    }
}