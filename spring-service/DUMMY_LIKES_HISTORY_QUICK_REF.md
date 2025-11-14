# Dummy Likes History Generator - Quick Reference

## Quick Start

### 1. Check Current Data
```bash
curl http://localhost:8080/api/dummy/likes-history/stats
```

### 2. Generate Dummy Data (Main Action)
```bash
curl -X POST http://localhost:8080/api/dummy/likes-history/generate
```

### 3. Verify Generation
```bash
curl http://localhost:8080/api/dummy/likes-history/stats
```

## What This Does

✅ Creates 50 potential customers (if needed)  
✅ Generates 5-25 random likes for EACH child post  
✅ Assigns timestamps from **post creation date to now** (realistic!)  
✅ Never creates likes before the post was created  
✅ Sets random reactions (1-5)  
✅ Uses realistic data distribution  
✅ Skips future posts automatically  

## API Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/dummy/likes-history/stats` | View current statistics |
| POST | `/api/dummy/likes-history/generate` | Generate dummy data |
| DELETE | `/api/dummy/likes-history/clear` | Clear all data (⚠️ dangerous) |

## Example Response

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

## Files Location

📁 **Service**: `src/main/java/com/itu/socialcom/demo/posts/services/dummy/DummyLikesHistoryService.java`  
📁 **Controller**: `src/main/java/com/itu/socialcom/demo/posts/controller/DummyLikesHistoryController.java`  
📁 **Test File**: `test_dummy_likes_history.http`  
📁 **Documentation**: `DUMMY_LIKES_HISTORY_API.md`  

## Important Notes

⚠️ Running `/generate` multiple times will ADD more likes (not replace)  
⚠️ `/clear` endpoint deletes ALL likes_history data  
✅ Customers are created only once (no duplicates)  
✅ All operations are transactional (atomic)  

## Testing with VS Code

1. Open `test_dummy_likes_history.http`
2. Click "Send Request" above any HTTP request
3. View response instantly

## Common Use Cases

### Initial Setup
```bash
# Run once to populate data
curl -X POST http://localhost:8080/api/dummy/likes-history/generate
```

### Reset and Regenerate
```bash
# Clear all
curl -X DELETE http://localhost:8080/api/dummy/likes-history/clear

# Generate fresh data
curl -X POST http://localhost:8080/api/dummy/likes-history/generate
```

### Check Progress
```bash
# Before
curl http://localhost:8080/api/dummy/likes-history/stats

# Generate
curl -X POST http://localhost:8080/api/dummy/likes-history/generate

# After
curl http://localhost:8080/api/dummy/likes-history/stats
```

## Troubleshooting

**Q: No data generated?**  
A: Check if you have any child posts in `post_childs` table

**Q: Error 500?**  
A: Check application logs for detailed error message

**Q: Want more/fewer likes?**  
A: Currently generates 5-25 per post. Modify `DummyLikesHistoryService.java` line with `5 + random.nextInt(21)` to adjust range

**Q: Clear only specific data?**  
A: Currently clears all. For selective deletion, use SQL or add a filtered endpoint

## Data Structure

```
potential_customers_v2 (50 customers)
├── facebook_user_00001 → facebook (id_sp: 1)
├── instagram_user_00002 → instagram (id_sp: 2)
├── x_user_00003 → x (id_sp: 3)
└── thread_user_00004 → thread (id_sp: 4)

post_childs (all existing)
├── id_child: 84 → gets 5-25 likes
├── id_child: 85 → gets 5-25 likes
└── ...

likes_history (generated)
├── id_lh: 1 → child: 84, customer: xxx, reactions: 3
├── id_lh: 2 → child: 84, customer: yyy, reactions: 1
└── ...
```

## Need Help?

See full documentation: `DUMMY_LIKES_HISTORY_API.md`
