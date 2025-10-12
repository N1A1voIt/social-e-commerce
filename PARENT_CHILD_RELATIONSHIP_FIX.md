# Parent-Child Relationship Fix for Facebook Posts

## Problem Identified

The `id_child_1` column in the `post_child` table is a self-reference that should link child posts to their parent/root child. For Facebook posts with sub-attachments (like albums with multiple photos), the child posts weren't properly linked to their root parent.

## Root Cause

### **Before Fix**
```java
// Incorrect logic in createNewPost()
int postMereId = -1;
for (PostChild postChild : post.getPostChildren()) {
    if (postMereId != -1) {
        postChild.setIdChild1(postMereId);  // Wrong parent ID
    }
    postChild.setIdPost(post.getId());
    postChildRepository.save(postChild);
    if (postChild.getType().equals("main_post")) {
        // postMereId was set AFTER saving, not before
    }
}
```

**Issues**:
1. `postMereId` was set to `-1` initially
2. Parent ID was set before the main post was saved and got its ID
3. Child posts were linked to the wrong parent or no parent at all

## Solution Implemented

### **Fixed Logic in createNewPost()**

```java
private void createNewPost(Post post, Seller seller, List<ManagedPageCPL> managedPageCPLS) {
    postRepository.save(post);
    
    // Step 1: Save all post children to get their IDs
    for (PostChild postChild : post.getPostChildren()) {
        postChild.setIdPost(post.getId());
        postChildRepository.save(postChild);
    }
    
    // Step 2: Find the main post child ID
    Integer mainPostChildId = null;
    for (PostChild postChild : post.getPostChildren()) {
        if ("main_post".equals(postChild.getType())) {
            mainPostChildId = postChild.getId();  // Get the actual ID after saving
            fetchAndSaveReactions(postChild, seller, managedPageCPLS);
        }
    }
    
    // Step 3: Set parent-child relationships
    for (PostChild postChild : post.getPostChildren()) {
        if (!"main_post".equals(postChild.getType())) {
            // Set the main post as parent for all child posts
            if (mainPostChildId != null) {
                postChild.setIdChild1(mainPostChildId);
                postChildRepository.save(postChild);
            }
        }
    }
}
```

### **Fixed Logic in updatePostChildren()**

```java
private void updatePostChildren(List<PostChild> newPostChildren, Integer postId, Seller seller, List<ManagedPageCPL> managedPageCPLS) {
    // Step 1: Get existing children and create new ones if needed
    Map<String, PostChild> existingChildrenMap = getExistingChildrenMap(postId);
    
    // Step 2: Save new children to get their IDs
    for (PostChild newChild : newPostChildren) {
        if (!existingChildrenMap.containsKey(newChild.getPlatformIdentifier())) {
            newChild.setIdPost(postId);
            postChildRepository.save(newChild);
            existingChildrenMap.put(newChild.getPlatformIdentifier(), newChild);
        }
    }
    
    // Step 3: Find the main post child ID
    Integer mainPostChildId = findMainPostChildId(newPostChildren, existingChildrenMap);
    
    // Step 4: Update children and set parent-child relationships
    for (PostChild newChild : newPostChildren) {
        PostChild existingChild = existingChildrenMap.get(newChild.getPlatformIdentifier());
        
        // Update fields if changed
        if (hasPostChildChanged(existingChild, newChild)) {
            updateChildFields(existingChild, newChild);
        }
        
        // Ensure parent-child relationship is set
        if (!"main_post".equals(newChild.getType()) && mainPostChildId != null) {
            if (!Objects.equals(existingChild.getIdChild1(), mainPostChildId)) {
                existingChild.setIdChild1(mainPostChildId);
                postChildRepository.save(existingChild);
            }
        }
    }
}
```

## Database Structure Understanding

### **post_child Table**
- **`id_child`**: Primary key (auto-generated)
- **`id_child_1`**: Self-reference to parent PostChild (nullable)
- **`id_post`**: References the main Post
- **`platform_identifier`**: Facebook post ID
- **`type`**: "main_post", "photo", "video", etc.

### **Parent-Child Relationship**
```
Post (id_post: 123)
├── PostChild (id_child: 1, type: "main_post", id_child_1: null)     ← ROOT
│   ├── PostChild (id_child: 2, type: "photo", id_child_1: 1)         ← CHILD
│   ├── PostChild (id_child: 3, type: "photo", id_child_1: 1)         ← CHILD
│   └── PostChild (id_child: 4, type: "video", id_child_1: 1)         ← CHILD
```

## Facebook Post Structure

### **Single Photo Post**
```
Post
└── PostChild (main_post) - contains message
    └── PostChild (photo) - contains photo URL
```

### **Album Post (Multiple Photos)**
```
Post
└── PostChild (main_post) - contains message
    ├── PostChild (photo) - photo 1
    ├── PostChild (photo) - photo 2
    └── PostChild (photo) - photo 3
```

## Instagram Posts (No Changes Needed)

Instagram posts have a different structure:
- **One PostChild per post** (the main post)
- **Multiple Media objects** linked to the single PostChild via `id_child`
- **No `id_child_1` relationships** needed

```java
// Instagram structure
Post
└── PostChild (main_post)
    ├── Media (photo 1)
    ├── Media (photo 2)
    └── Media (photo 3)
```

## Benefits of the Fix

✅ **Correct Parent-Child Relationships**: Child posts are properly linked to their main post  
✅ **Proper Data Structure**: Follows the intended database design  
✅ **Consistent Updates**: Parent-child relationships are maintained during updates  
✅ **Better Query Performance**: Can efficiently query parent-child relationships  
✅ **Data Integrity**: Ensures referential integrity in the database  

## Testing Scenarios

### **Test Cases**
1. **Single Photo Post**: Verify main_post → photo relationship
2. **Album Post**: Verify main_post → multiple photos relationships
3. **Mixed Media Post**: Verify main_post → photos + videos relationships
4. **Update Existing Post**: Verify relationships are maintained during updates
5. **New Child Addition**: Verify new children are properly linked

### **Verification Queries**
```sql
-- Check parent-child relationships
SELECT 
    pc1.id_child as parent_id,
    pc1.type as parent_type,
    pc1.platform_identifier as parent_platform_id,
    pc2.id_child as child_id,
    pc2.type as child_type,
    pc2.platform_identifier as child_platform_id
FROM post_childs pc1
LEFT JOIN post_childs pc2 ON pc1.id_child = pc2.id_child_1
WHERE pc1.id_post = ? AND pc1.id_sp = 1
ORDER BY pc1.id_child, pc2.id_child;
```

## Logging Output

### **New Posts**
```
Created new Facebook post: 123 with 4 children
```

### **Updated Posts**
```
Updated existing Facebook post: 123 (platform_identifier: 789_456)
Detected changes in Facebook post child: 789_456
Facebook post 123 was updated with changes
```

This fix ensures that Facebook posts with sub-attachments (like photo albums) have proper parent-child relationships in the database, making the data structure consistent and queryable.
