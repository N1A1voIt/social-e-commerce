# Create Post From Preview Feature Implementation

## Overview
Implemented the `/api/posts/create-post-from-preview` endpoint that creates posts across multiple platforms and pages based on preview data sent from the frontend.

## Key Concept
The feature allows posting the same content to **all pages belonging to the same platform**. For example:
- If you have 3 Facebook pages and 2 Instagram pages
- And you send preview data for Facebook and Instagram
- The system will create the Facebook post on all 3 Facebook pages
- And create the Instagram post on all 2 Instagram pages

## Files Created

### 1. DTOs (Data Transfer Objects)

#### MediaDetailPreview.java
```java
@Data
public class MediaDetailPreview {
    String imageUrl;
    String message;
}
```
Represents a single media item with its URL and message.

#### PlatformPreviewItem.java
```java
@Data
public class PlatformPreviewItem {
    String platform;           // e.g., "facebook", "instagram"
    String mainMessage;        // Main post message for this platform
    List<MediaDetailPreview> mediaDetails;  // Media items for this platform
}
```
Contains the post content for a specific platform.

#### CreatePostFromPreviewArgs.java
```java
@Data
public class CreatePostFromPreviewArgs {
    List<PlatformPreviewItem> platformPreviews;  // Post content per platform
    List<PageDetails> pageDetails;               // List of pages to post to
    List<Long> idProducts;                       // Optional: associated products
    Long scheduledUnixTime;                      // Optional: for scheduling
}
```
Request payload for the endpoint.

### 2. Service Layer

#### PostFromPreviewSaver.java
Core service that handles the post creation logic:

**Key Features:**
- Groups pages by platform
- Matches platform previews with the corresponding pages
- Creates posts for each page of the same platform using the same content
- Handles media upload for each page
- Supports both immediate and scheduled posting
- Saves all posts with proper parent-child relationships

**Main Method:**
```java
@Transactional
public List<PostChild> createPostsFromPreview(CreatePostFromPreviewArgs args, Seller seller)
```

**Logic Flow:**
1. Retrieve all managed pages for the seller
2. Group page details by platform (e.g., all Facebook pages together)
3. For each platform:
   - Get the corresponding platform preview content
   - For each page of that platform:
     - Upload all media items
     - Create the post with the uploaded media
4. Save the mother post and all child posts with relationships
5. Return list of created posts

### 3. Controller Update

#### PostController.java
Added endpoint:
```java
@PostMapping("/create-post-from-preview")
public ResponseEntity<?> createPostFromPreview(
    @RequestBody CreatePostFromPreviewArgs args, 
    @RequestHeader("Authorization") String token)
```

## API Usage

### Endpoint
`POST /api/posts/create-post-from-preview`

### Request Headers
```
Authorization: <seller-token>
Content-Type: application/json
```

### Request Body Example
```json
{
  "platformPreviews": [
    {
      "platform": "facebook",
      "mainMessage": "Check out our new product!",
      "mediaDetails": [
        {
          "imageUrl": "https://example.com/image1.jpg",
          "message": "Product view 1"
        },
        {
          "imageUrl": "https://example.com/image2.jpg",
          "message": "Product view 2"
        }
      ]
    },
    {
      "platform": "instagram",
      "mainMessage": "New arrival! 🎉",
      "mediaDetails": [
        {
          "imageUrl": "https://example.com/image1.jpg",
          "message": "Product showcase"
        }
      ]
    }
  ],
  "pageDetails": [
    {
      "pageId": "facebook-page-1-id",
      "platform": "facebook"
    },
    {
      "pageId": "facebook-page-2-id",
      "platform": "facebook"
    },
    {
      "pageId": "instagram-page-1-id",
      "platform": "instagram"
    }
  ],
  "idProducts": [1, 2, 3],
  "scheduledUnixTime": null
}
```

### Response
Returns a list of created `PostChild` objects with details about each post created.

### Behavior
With the above request:
- Creates 2 Facebook posts (one on each Facebook page) with the Facebook preview content
- Creates 1 Instagram post on the Instagram page with the Instagram preview content
- All posts share the same "mother" post record for tracking

## Features

✅ **Multi-platform support**: Posts to different social media platforms
✅ **Multi-page support**: Posts to all pages of the same platform with one request
✅ **Platform-specific content**: Each platform can have different content/messaging
✅ **Media handling**: Uploads and attaches media for each platform
✅ **Scheduling support**: Can schedule posts for future publishing
✅ **Product linking**: Can associate products with posts
✅ **Error handling**: Graceful failure handling per page/platform
✅ **Transaction safety**: Entire operation wrapped in @Transactional

## Error Handling

- **Invalid token**: Returns 401
- **Seller not found**: Returns 400
- **Missing page**: Skips that page and continues with others
- **Missing platform preview**: Skips that platform and continues
- **Media upload failure**: Logs error and continues
- **Post creation failure**: Logs error and continues with other pages

## Notes

- The service uses existing `SavePostService` implementations via `SaverFactory`
- Media is uploaded as "unpublished" first, then published with the post
- Each platform's post can have different content, images, and messages
- The system maintains relationships: Mother Post → Post Children → Media Children

