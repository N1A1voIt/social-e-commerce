# Dummy Likes History Generator - API Documentation

## Overview

This feature provides REST API endpoints to generate dummy `likes_history` data for all child posts in the database. It automatically creates potential customers and assigns random likes to each post with realistic timestamps distributed over the past 6 months.

## Files Created

1. **Service**: `src/main/java/com/itu/socialcom/demo/posts/services/dummy/DummyLikesHistoryService.java`
   - Core business logic for generating dummy data
   - Handles potential customer creation
   - Manages likes generation with random timestamps and reactions

2. **Controller**: `src/main/java/com/itu/socialcom/demo/posts/controller/DummyLikesHistoryController.java`
   - REST API endpoints
   - Request/response handling
   - Error handling

3. **Test File**: `test_dummy_likes_history.http`
   - HTTP requests for testing the API
   - Can be used with VS Code REST Client or IntelliJ HTTP Client

## API Endpoints

### 1. Generate Dummy Likes History

**Endpoint**: `POST /api/dummy/likes-history/generate`

**Description**: Generates dummy likes_history data for all child posts in the database.

**Process**:
- Creates 50 potential customers (if not already existing) across all platforms
- For each child post, generates 5-25 random likes
- Assigns random timestamps **between the post creation date and now** (realistic!)
- Uses child post's `created_time` or parent post's `create_at` for date reference
- Skips posts created in the future
- Sets random reaction counts (1-5) for each like

**Request**:
```http
POST http://localhost:8080/api/dummy/likes-history/generate
Content-Type: application/json
```

**Response**:
```json
{
  "success": true,
  "message": "Successfully generated dummy likes_history data",
  "data": {
    "childPostsProcessed": 41,
    "likesGenerated": 512,
    "customersUsed": 50,
    "averageLikesPerPost": 12.48
  }
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Failed to generate dummy likes_history data: [error message]",
  "error": "ExceptionType"
}
```

### 2. Get Statistics

**Endpoint**: `GET /api/dummy/likes-history/stats`

**Description**: Retrieves current statistics about likes_history data.

**Request**:
```http
GET http://localhost:8080/api/dummy/likes-history/stats
Content-Type: application/json
```

**Response**:
```json
{
  "success": true,
  "data": {
    "totalChildPosts": 41,
    "totalLikes": 512,
    "totalCustomers": 50,
    "averageLikesPerPost": 12.48
  }
}
```

### 3. Clear All Likes History

**Endpoint**: `DELETE /api/dummy/likes-history/clear`

**Description**: Deletes all likes_history records (useful for testing/resetting).

**⚠️ WARNING**: This will delete ALL likes_history data including real data!

**Request**:
```http
DELETE http://localhost:8080/api/dummy/likes-history/clear
Content-Type: application/json
```

**Response**:
```json
{
  "success": true,
  "message": "Successfully cleared all likes_history data",
  "data": {
    "deletedRecords": 512
  }
}
```

## Usage Examples

### Using cURL

```bash
# Generate dummy data
curl -X POST http://localhost:8080/api/dummy/likes-history/generate

# Get statistics
curl -X GET http://localhost:8080/api/dummy/likes-history/stats

# Clear all data (use with caution!)
curl -X DELETE http://localhost:8080/api/dummy/likes-history/clear
```

### Using the HTTP Test File

1. Open `test_dummy_likes_history.http` in VS Code or IntelliJ IDEA
2. Click "Send Request" above any request
3. View the response in the output panel

### Using Postman

Import the following requests:

**Generate Data**:
- Method: POST
- URL: `http://localhost:8080/api/dummy/likes-history/generate`
- Headers: `Content-Type: application/json`

**Get Stats**:
- Method: GET
- URL: `http://localhost:8080/api/dummy/likes-history/stats`

## Data Generation Details

### Potential Customers
- **Count**: 50 customers created (if they don't exist)
- **Distribution**: Evenly distributed across 4 platforms (Facebook, Instagram, X, Thread)
- **Names**: Randomly selected from a pool of common first names
- **Identifiers**: Format: `{platform}_user_{number}` (e.g., `facebook_user_00001`)
- **Profile URLs**: Generated based on platform and identifier
- **Avatars**: Placeholder images with user numbers

### Likes Generation
- **Quantity per post**: 5-25 random likes per child post
- **Timestamps**: Randomly distributed between **post creation date and now** (realistic!)
  - Checks `PostChild.created_time` first
  - Falls back to parent `Post.create_at` if child time not available
  - Uses 6 months ago as last resort fallback
  - Never generates likes before post was created
  - Skips posts with future creation dates
- **Reactions**: Random count between 1-5 (simulating different reaction types)
- **Customer assignment**: Random selection from available customers

### Database Impact
- Creates records in `potential_customers_v2` table (if needed)
- Creates records in `likes_history` table
- No modification to existing `post_childs` data

## Technical Details

### Service Methods

#### `generateDummyLikesHistory()`
- Main method to generate all dummy data
- Returns `DummyDataGenerationResult` with statistics
- Transactional operation (all-or-nothing)

#### `ensurePotentialCustomers()`
- Checks existing customer count
- Creates additional customers if needed (target: 50 total)
- Returns list of all available customers

#### `getStats()`
- Calculates and returns current database statistics
- No data modification

#### `clearAllLikesHistory()`
- Deletes all records from likes_history table
- Returns count of deleted records
- **USE WITH CAUTION**

### Dependencies
- Spring Boot
- JPA/Hibernate
- PostgreSQL
- Lombok

## Testing Strategy

1. **Check Initial State**: Call GET `/stats` to see current data
2. **Generate Data**: Call POST `/generate` to create dummy data
3. **Verify Results**: Call GET `/stats` again to confirm generation
4. **Optional Reset**: Call DELETE `/clear` to remove all data for fresh testing

## Error Handling

All endpoints include try-catch blocks and return structured error responses:
- HTTP 200: Success
- HTTP 500: Internal Server Error with detailed message

## Logging

The service uses SLF4J logging:
- INFO: Major operations (start, completion, counts)
- DEBUG: Detailed per-post generation info
- WARN: Edge cases (no posts found, etc.)
- ERROR: Exceptions with stack traces

## Notes

- The generation is **idempotent** for customers (won't create duplicates)
- Running generate multiple times will **add more likes** to existing posts
- Timestamps are generated with UTC timezone
- The service is thread-safe due to `@Transactional` annotation

## Future Enhancements

Potential improvements:
- Configurable number of likes per post (via request parameters)
- Configurable time range for timestamps
- Batch processing for large datasets
- More sophisticated reaction type distribution
- Weighted customer selection (some customers more active than others)
- Date range filtering for clearing data
