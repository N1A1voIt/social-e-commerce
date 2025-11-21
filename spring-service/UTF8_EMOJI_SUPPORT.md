# UTF-8 Emoji and Accent Support for Social Media Post Savers

## Overview

This document describes the UTF-8 encoding improvements made to `FacebookPostSaver` and `InstagramPostSaver` to support emojis and accented characters in social media posts.

## Problem

Previously, both post savers were using Apache HttpClient's default ISO-8859-1 encoding, which doesn't support:
- Emojis (🚀, 😊, ❤️, etc.)
- Unicode characters (é, ñ, ç, etc.)
- Special symbols (✨, 🛍️, 📱, etc.)

This caused text corruption when posting content with these characters to Facebook and Instagram.

## Solution

### 1. Text Body Encoding
Updated all `MultipartEntityBuilder.addTextBody()` calls to explicitly use UTF-8 encoding:

```java
// Before (uses default ISO-8859-1)
builder.addTextBody("message", message);

// After (uses UTF-8)
ContentType utf8Text = ContentType.create("text/plain", "UTF-8");
builder.addTextBody("message", message, utf8Text);
```

### 2. Response Parsing
Updated all `EntityUtils.toString()` calls to use UTF-8 encoding:

```java
// Before (uses default charset)
String json = EntityUtils.toString(response.getEntity());

// After (uses UTF-8)
String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
```

## Changes Made

### FacebookPostSaver.java
- Added `ContentType` import
- Added `StandardCharsets` import
- Updated `uploadMediaUnpublished()` method
- Updated `createPostWithMedia()` method
- Updated `schedulePostWithMedia()` method
- Removed unused imports (`Transactional`, `Arrays`)

### InstagramPostSaver.java
- Added `ContentType` import
- Added `StandardCharsets` import
- Updated `uploadMediaUnpublished()` method
- Updated `createPostWithMedia()` method (single and carousel posts)
- Updated `schedulePostWithMedia()` method (single and carousel posts)

## Supported Characters

The implementation now supports:

### Emojis
- 😊 😍 😎 🤔 😂 🥰 😇 🤗 🙌 👏
- 🚀 ✨ 🎉 🎊 🎁 💝 🛍️ 💎 🔥 ⚡
- ❤️ 💕 💖 💗 💙 💚 💛 🧡 💜 🖤
- 👍 👎 ✅ ❌ ⭐ 🌟 💯 🔝 📈 📊

### Accented Characters
- French: é, è, ê, ë, à, â, ä, ù, û, ü, ô, ö, ç, î, ï, ÿ
- Spanish: á, é, í, ó, ú, ñ, ü, ¿, ¡
- German: ä, ö, ü, ß
- Italian: à, è, é, ì, í, ò, ó, ù, ú

### Special Symbols
- Currency: €, £, ¥, ₹, $, ¢
- Math: ±, ×, ÷, ≈, ≠, ≤, ≥, ∞
- Arrows: ← ↑ → ↓ ↔ ↕ ⤴ ⤵
- Punctuation: – — … « » " " ' '

## Testing

A comprehensive test suite (`PostSaverEncodingTest.java`) has been created to verify:

1. UTF-8 ContentType creation
2. Emoji preservation through encoding/decoding
3. Accented character support
4. Common social media character handling

## Example Usage

Now you can post content like:

```java
String message = "🌟 NEW ARRIVAL! 🌟\n" +
                "Café Deluxe Collection ☕\n" +
                "✨ Très élégant! ✨\n" +
                "🛍️ Shop now: café-boutique.com\n" +
                "#NewCollection #Café #Élégance 💎";

// This will work correctly with both Facebook and Instagram
postSaver.createPostWithMedia(postDetails);
```

## Compatibility

- ✅ Facebook Graph API v20.0+
- ✅ Instagram Graph API v23.0+
- ✅ All major browsers and mobile apps
- ✅ Spring Boot 3.x
- ✅ Apache HttpClient 5.x

## Notes

- UTF-8 is the standard encoding for modern web APIs
- Both Facebook and Instagram APIs expect UTF-8 encoded text
- This change is backward compatible (ASCII characters work the same)
- Performance impact is negligible

## Testing the Implementation

To test that emojis and accents work:

1. Run the unit tests: `mvn test -Dtest=PostSaverEncodingTest`
2. Create a test post with emojis and accented characters
3. Verify the post appears correctly on Facebook/Instagram

Example test message:
```
🚀 Bonjour! Comment ça va? 😊
✨ Café & Crêpes disponibles! 🥐☕
📱 Visit notre site: café-boutique.com 🔗
#Français #Café #Délicieux 💯
```