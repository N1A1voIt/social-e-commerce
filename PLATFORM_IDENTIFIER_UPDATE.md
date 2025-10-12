# Posts Loading Update - Platform Identifier Based Logic

## Overview

Updated the `/api/posts/loads` endpoint to properly handle existing posts based on the `platform_identifier` column in the `post_child` table, where:

- **`platform_identifier`** = The actual post ID from Facebook/Instagram
- **`id_sp`** = Supported platform (1 for Facebook, 2 for Instagram)

## Key Changes Made

### 1. **FacebookPostRetrieval.java**

#### **Extract Post Data**
- **Before**: Skipped existing posts entirely (`if (postIdentifiers.contains(...)) continue;`)
- **After**: Includes all posts and marks them as existing (`postData.put("isExisting", postIdentifiers.contains(facebookPostId))`)

#### **Transform Post**
- Added `isExisting` flag handling to track whether posts already exist in the database

#### **Load Post**
- **Before**: Always created new posts
- **After**: 
  - **New posts**: Creates new posts with `createNewPost()`
  - **Existing posts**: Updates existing posts with `updateExistingPost()`

#### **Update Logic**
- Finds existing posts by `platform_identifier` (Facebook post ID)
- Updates only changed fields (description, URLs, media, type)
- Handles post children updates based on `platform_identifier`
- Provides detailed logging of changes

### 2. **InstagramPostRetrieval.java**

#### **Extract Post Data**
- **Before**: Skipped existing posts entirely
- **After**: Includes all posts and marks them as existing (`postData.put("isExisting", postIdentifiers.contains(instagramPostId))`)

#### **Transform Post**
- Added `isExisting` flag handling

#### **Load Post**
- **Before**: Always created new posts
- **After**:
  - **New posts**: Creates new posts with `createNewInstagramPost()`
  - **Existing posts**: Updates existing posts with `updateExistingInstagramPost()`

#### **Update Logic**
- Finds existing posts by `platform_identifier` (Instagram post ID)
- Updates post content and media
- Handles media changes by replacing existing media with new media

### 3. **Post.java Entity**

#### **Added Field**
```java
@Transient
Boolean isExisting;
```

This field tracks whether a post already exists in the database during processing.

## Database Schema Understanding

### **post_child Table Structure**
- **`platform_identifier`**: The actual post ID from the social media platform
  - Facebook: `"123456789_987654321"` (page_post format)
  - Instagram: `"17841400000000000"` (Instagram media ID)
- **`id_sp`**: Supported platform identifier
  - `1` = Facebook
  - `2` = Instagram

### **Query Logic**
```java
// Get existing Facebook post IDs
Set<String> postIdentifiers = postChildRepository.findDistinctPlatformIdentifierByIdSp(1L);

// Get existing Instagram post IDs  
Set<String> postIdentifiers = postChildRepository.findDistinctPlatformIdentifierByIdSp(2L);
```

## Behavior Changes

### **Before**
1. Fetch posts from Facebook/Instagram API
2. Skip posts that already exist (`continue` statement)
3. Create new posts for non-existing posts
4. **Result**: No updates to existing posts, potential data staleness

### **After**
1. Fetch posts from Facebook/Instagram API
2. Mark all posts as existing or new based on `platform_identifier`
3. **New posts**: Create new posts
4. **Existing posts**: Update only if changes detected
5. **Result**: Fresh data with intelligent updates

## Update Detection

### **Facebook Posts**
- Compares: description, post URL, type, media URL
- Updates: Only changed fields
- Logs: Detailed change information

### **Instagram Posts**
- Compares: description, permalink, media type
- Updates: Post content and media
- Media handling: Replaces existing media with new media

## Logging Output

### **New Posts**
```
Created new Facebook post: 123
Created new Instagram post: 456
```

### **Updated Posts**
```
Updated existing Facebook post: 123 (platform_identifier: 789_456)
Detected changes in Facebook post child: 789_456
Facebook post 123 was updated with changes
```

### **No Changes**
```
Facebook post 123 has no changes
```

## Benefits

✅ **No Duplicates**: Existing posts are updated instead of skipped  
✅ **Fresh Data**: Posts are updated when they change on social media  
✅ **Efficient**: Only updates posts that have actually changed  
✅ **Reliable**: Uses platform identifiers for accurate matching  
✅ **Transparent**: Detailed logging shows what's happening  

## Technical Implementation

### **Change Detection**
- Uses `Objects.equals()` for safe null comparison
- Compares all relevant fields
- Only updates when actual changes are detected

### **Database Operations**
- **Create**: `postRepository.save()` + `postChildRepository.save()`
- **Update**: `postRepository.save()` + selective field updates
- **Media**: Handles media updates appropriately for each platform

### **Error Handling**
- Graceful handling of missing posts
- Detailed error logging
- Continues processing other posts if one fails

This implementation ensures that the `/api/posts/loads` endpoint now properly handles existing posts based on the `platform_identifier` column, providing fresh data while avoiding duplicates.
