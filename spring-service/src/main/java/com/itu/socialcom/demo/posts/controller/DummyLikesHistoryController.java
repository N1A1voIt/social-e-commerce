package com.itu.socialcom.demo.posts.controller;

import com.itu.socialcom.demo.posts.services.dummy.DummyLikesHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dummy/likes-history")
public class DummyLikesHistoryController {

    @Autowired
    private DummyLikesHistoryService dummyLikesHistoryService;

    /**
     * Generate dummy likes_history data for all child posts
     * 
     * POST /api/dummy/likes-history/generate
     * 
     * @return Summary of generated data including number of posts processed, likes generated, and customers used
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateDummyLikesHistory() {
        try {
            log.info("API call received to generate dummy likes_history data");
            
            DummyLikesHistoryService.DummyDataGenerationResult result = 
                dummyLikesHistoryService.generateDummyLikesHistory();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully generated dummy likes_history data");
            response.put("data", Map.of(
                "childPostsProcessed", result.childPostsProcessed,
                "likesGenerated", result.likesGenerated,
                "customersUsed", result.customersUsed,
                "averageLikesPerPost", result.childPostsProcessed > 0 
                    ? (double) result.likesGenerated / result.childPostsProcessed 
                    : 0
            ));

            log.info("Successfully generated {} likes for {} posts", 
                result.likesGenerated, result.childPostsProcessed);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating dummy likes_history data", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to generate dummy likes_history data: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get statistics about current likes_history data
     * 
     * GET /api/dummy/likes-history/stats
     * 
     * @return Statistics including total posts, likes, customers, and averages
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            log.info("API call received to get likes_history stats");
            
            DummyLikesHistoryService.DummyDataStats stats = 
                dummyLikesHistoryService.getStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "totalChildPosts", stats.totalChildPosts,
                "totalLikes", stats.totalLikes,
                "totalCustomers", stats.totalCustomers,
                "averageLikesPerPost", stats.avgLikesPerChild
            ));

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting likes_history stats", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get stats: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Clear all likes_history data (useful for testing/resetting)
     * 
     * DELETE /api/dummy/likes-history/clear
     * 
     * @return Number of records deleted
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearLikesHistory() {
        try {
            log.info("API call received to clear all likes_history data");
            
            int deletedCount = dummyLikesHistoryService.clearAllLikesHistory();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully cleared all likes_history data");
            response.put("data", Map.of(
                "deletedRecords", deletedCount
            ));

            log.info("Successfully deleted {} likes_history records", deletedCount);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error clearing likes_history data", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to clear likes_history data: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
