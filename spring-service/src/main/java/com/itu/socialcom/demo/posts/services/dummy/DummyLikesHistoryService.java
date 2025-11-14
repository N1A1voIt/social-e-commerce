package com.itu.socialcom.demo.posts.services.dummy;

import com.itu.socialcom.demo.posts.entity.LikesHistory;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.repository.LikesHistoryRepository;
import com.itu.socialcom.demo.posts.repository.PostChildRepository;
import com.itu.socialcom.demo.posts.repository.PostRepository;
import com.itu.socialcom.demo.potentialCustomers.entity.PotentialCustomerV2;
import com.itu.socialcom.demo.potentialCustomers.repository.PotentialCustomerV2Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class DummyLikesHistoryService {

    @Autowired
    private LikesHistoryRepository likesHistoryRepository;

    @Autowired
    private PostChildRepository postChildRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PotentialCustomerV2Repository potentialCustomerRepository;

    private final Random random = new Random();

    /**
     * Generates dummy likes_history data for all child posts
     * Creates potential customers if needed and assigns random likes to each post
     * Ensures likes timestamps are AFTER the post creation date for realism
     *
     * @return Summary of generated data
     */
    @Transactional
    public DummyDataGenerationResult generateDummyLikesHistory() {
        log.info("Starting dummy likes_history generation");

        // Step 1: Ensure we have potential customers
        List<PotentialCustomerV2> customers = ensurePotentialCustomers();
        log.info("Available potential customers: {}", customers.size());

        // Step 2: Get all child posts with their parent posts
        List<PostChild> allChildPosts = postChildRepository.findAll();
        log.info("Found {} child posts", allChildPosts.size());

        if (allChildPosts.isEmpty()) {
            log.warn("No child posts found. Returning empty result.");
            return new DummyDataGenerationResult(0, 0, customers.size());
        }

        // Step 3: Get all posts for date reference
        Map<Integer, Post> postsMap = new HashMap<>();
        for (Post post : postRepository.findAll()) {
            postsMap.put(post.getId(), post);
        }

        // Step 4: Generate likes for each child post
        int totalLikesGenerated = 0;
        int skippedPosts = 0;

        for (PostChild child : allChildPosts) {
            // Determine the post creation date
            LocalDateTime postCreationDate = determinePostCreationDate(child, postsMap);
            
            if (postCreationDate == null) {
                log.warn("Could not determine creation date for child post ID: {}. Using fallback.", child.getId());
                postCreationDate = LocalDateTime.now().minusMonths(6);
            }

            // Ensure we don't generate likes for future posts
            LocalDateTime now = LocalDateTime.now();
            if (postCreationDate.isAfter(now)) {
                log.debug("Skipping child post ID: {} (created in the future)", child.getId());
                skippedPosts++;
                continue;
            }

            // Random number of likes between 5 and 25
            int numLikes = 5 + random.nextInt(21);

            // Get random customers for this post
            List<PotentialCustomerV2> selectedCustomers = getRandomCustomers(customers, numLikes);

            // Generate likes with timestamps AFTER post creation
            for (PotentialCustomerV2 customer : selectedCustomers) {
                LikesHistory like = new LikesHistory();
                like.setIdChild(child.getId());
                
                // Random timestamp between post creation and now
                LocalDateTime randomTimestamp = generateRandomTimestamp(postCreationDate, now);
                like.setCreatedAt(randomTimestamp);
                
                // Random reaction count (1-5)
                like.setReactions(25 + random.nextInt(40,150));

                likesHistoryRepository.save(like);
                totalLikesGenerated++;
            }

            log.debug("Generated {} likes for child post ID: {} (created at: {})", 
                numLikes, child.getId(), postCreationDate);
        }

        log.info("Successfully generated {} likes for {} child posts (skipped {} future posts)", 
            totalLikesGenerated, allChildPosts.size() - skippedPosts, skippedPosts);

        return new DummyDataGenerationResult(
                allChildPosts.size() - skippedPosts,
                totalLikesGenerated,
                customers.size()
        );
    }

    /**
     * Determines the creation date for a post, checking multiple sources
     * Priority: PostChild.createdTime -> Post.createAt -> fallback (6 months ago)
     */
    private LocalDateTime determinePostCreationDate(PostChild child, Map<Integer, Post> postsMap) {
        // First check if the child has a created_time
        if (child.getCreatedTime() != null) {
            log.debug("Using child post created_time for ID: {}", child.getId());
            return child.getCreatedTime();
        }

        // Then check the parent post's create_at
        Post parentPost = postsMap.get(child.getIdPost());
        if (parentPost != null && parentPost.getCreateAt() != null) {
            log.debug("Using parent post create_at for child ID: {}", child.getId());
            return parentPost.getCreateAt();
        }

        // Fallback to 6 months ago if no date is available
        log.debug("No creation date found for child ID: {}, using fallback", child.getId());
        return LocalDateTime.now().minusMonths(6);
    }

    /**
     * Ensures there are at least 50 potential customers in the database
     * Creates them if they don't exist
     */
    private List<PotentialCustomerV2> ensurePotentialCustomers() {
        List<PotentialCustomerV2> existingCustomers = potentialCustomerRepository.findAll();

        // If we have enough customers, return them
        if (existingCustomers.size() >= 50) {
            return existingCustomers;
        }

        log.info("Creating additional potential customers. Current count: {}", existingCustomers.size());

        // Create additional customers
        String[] platforms = {"facebook", "instagram", "x", "thread"};
        Long[] platformIds = {1L, 2L, 3L, 4L};
        String[] firstNames = {"John", "Jane", "Michael", "Sarah", "Alex", "Emma", "David", "Olivia", "James", "Sophia"};

        int customersToCreate = 50 - existingCustomers.size();
        List<PotentialCustomerV2> newCustomers = new ArrayList<>();

        for (int i = 0; i < customersToCreate; i++) {
            int platformIndex = i % 4;
            String platform = platforms[platformIndex];
            Long platformId = platformIds[platformIndex];

            String firstName = firstNames[random.nextInt(firstNames.length)];
            String uniqueId = platform + "_user_" + String.format("%05d", existingCustomers.size() + i + 1);

            PotentialCustomerV2 customer = new PotentialCustomerV2();
            customer.setName(firstName + " " + (existingCustomers.size() + i + 1));
            customer.setPlatform(platform);
            customer.setSupportedPlatform(platformId);
            customer.setIdentifierOnPlatform(uniqueId);
            customer.setLinkToProfile("https://" + platform + ".com/" + uniqueId);
            customer.setMediaUrl("https://via.placeholder.com/150?text=User" + (existingCustomers.size() + i + 1));

            PotentialCustomerV2 saved = potentialCustomerRepository.save(customer);
            newCustomers.add(saved);
        }

        log.info("Created {} new potential customers", newCustomers.size());

        // Combine existing and new customers
        existingCustomers.addAll(newCustomers);
        return existingCustomers;
    }

    /**
     * Selects random customers from the list
     */
    private List<PotentialCustomerV2> getRandomCustomers(List<PotentialCustomerV2> allCustomers, int count) {
        if (count >= allCustomers.size()) {
            return new ArrayList<>(allCustomers);
        }

        List<PotentialCustomerV2> shuffled = new ArrayList<>(allCustomers);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, count);
    }

    /**
     * Generates a random timestamp between start and end
     */
    private LocalDateTime generateRandomTimestamp(LocalDateTime start, LocalDateTime end) {
        long startSeconds = start.toEpochSecond(java.time.ZoneOffset.UTC);
        long endSeconds = end.toEpochSecond(java.time.ZoneOffset.UTC);
        long randomSeconds = startSeconds + (long) (random.nextDouble() * (endSeconds - startSeconds));
        return LocalDateTime.ofEpochSecond(randomSeconds, 0, java.time.ZoneOffset.UTC);
    }

    /**
     * Clears all dummy likes_history data (useful for testing)
     */
    @Transactional
    public int clearAllLikesHistory() {
        log.info("Clearing all likes_history data");
        int count = (int) likesHistoryRepository.count();
        likesHistoryRepository.deleteAll();
        log.info("Deleted {} likes_history records", count);
        return count;
    }

    /**
     * Gets statistics about current likes_history data
     */
    public DummyDataStats getStats() {
        long totalLikes = likesHistoryRepository.count();
        long totalChildren = postChildRepository.count();
        long totalCustomers = potentialCustomerRepository.count();

        double avgLikesPerChild = totalChildren > 0 ? (double) totalLikes / totalChildren : 0;

        return new DummyDataStats(
                totalChildren,
                totalLikes,
                totalCustomers,
                avgLikesPerChild
        );
    }

    /**
     * Result DTO for generation operation
     */
    public static class DummyDataGenerationResult {
        public final int childPostsProcessed;
        public final int likesGenerated;
        public final int customersUsed;

        public DummyDataGenerationResult(int childPostsProcessed, int likesGenerated, int customersUsed) {
            this.childPostsProcessed = childPostsProcessed;
            this.likesGenerated = likesGenerated;
            this.customersUsed = customersUsed;
        }
    }

    /**
     * Stats DTO
     */
    public static class DummyDataStats {
        public final long totalChildPosts;
        public final long totalLikes;
        public final long totalCustomers;
        public final double avgLikesPerChild;

        public DummyDataStats(long totalChildPosts, long totalLikes, long totalCustomers, double avgLikesPerChild) {
            this.totalChildPosts = totalChildPosts;
            this.totalLikes = totalLikes;
            this.totalCustomers = totalCustomers;
            this.avgLikesPerChild = avgLikesPerChild;
        }
    }
}
