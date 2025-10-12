package com.itu.socialcom.demo.posts.services.etl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.dto.ExtractorArgs;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.entity.LikesHistory;
import com.itu.socialcom.demo.posts.repository.LikesHistoryRepository;

import com.itu.socialcom.demo.posts.repository.VRefreshTokenHolderRepository;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Optional;

@Service
public class FacebookPostRetrieval extends PostRetrievalSignature{
    @Autowired
    VRefreshTokenHolderRepository vRefreshTokenHolderRepository;
//    private
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LikesHistoryRepository likesHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ManagedPageCPLRepository managedPageCPLRepository;

    private static final String FACEBOOK_API_BASE_URL = "https://graph.facebook.com/v20.0";
    private static final String POSTS_ENDPOINT = "/posts";
    private static final String FIELDS = "id,message,permalink_url,attachments{media_type,url,media,subattachments},created_time,reactions.summary(total_count)";
    private static final String REACTIONS_FIELDS = "id,name,type,created_time";

    @Override
    public Map<String, Object> extractPostData(ExtractorArgs args) {
        Seller seller = args.getSeller();
        Map<String, Object> extractedData = new HashMap<>();
        Set<String> postIdentifiers = postChildRepository.findDistinctPlatformIdentifierByIdSp(1L);
        List<Map<String, Object>> allPostsData = new ArrayList<>();

        List<ManagedPageCPL> managedPageCPLS = managedPageCPLRepository.findByIdSellerAndPlatform(seller.getId(), "facebook");


        for (ManagedPageCPL tokenHolder : managedPageCPLS) {
            try {
                String pageId = tokenHolder.getPlatformIdentifier();
                String accessToken = tokenHolder.getRefreshToken();

                // Build URI with proper encoding using UriComponentsBuilder
                URI uri = UriComponentsBuilder.fromHttpUrl(FACEBOOK_API_BASE_URL)
                        .pathSegment(pageId)  // Handle pageId as a path segment
                        .path(POSTS_ENDPOINT) // Append the endpoint path (e.g., "/posts")
                        .queryParam("fields", FIELDS) // Automatically encodes special characters
                        .queryParam("access_token", accessToken) // Encodes token if needed
                        .build()
                        .toUri();

                // Fetch data using the properly encoded URI
                String response = restTemplate.getForObject(uri, String.class);

                // Process response (unchanged)
                if (response != null) {
                    JsonNode jsonResponse = objectMapper.readTree(response);
                    JsonNode dataNode = jsonResponse.get("data");
                    if (dataNode != null && dataNode.isArray()) {
                        for (JsonNode postNode : dataNode) {
                            String facebookPostId = postNode.get("id").asText();
                            Map<String, Object> postData = new HashMap<>();
                            postData.put("pageId", pageId);
                            postData.put("postNode", postNode);
                            postData.put("sellerId", seller.getId());
                            postData.put("isExisting", postIdentifiers.contains(facebookPostId));
                            allPostsData.add(postData);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error extracting data for token holder: " + e.getMessage());
            }
        }

        extractedData.put("postsData", allPostsData);
        return extractedData;
    }
    @Override
    public List<Post> transformPost(ExtractorArgs args) {
        Map<String,Object> extractedData = this.extractPostData(args);
        if (extractedData == null || extractedData.isEmpty()) {
            throw new IllegalArgumentException("extracted data is null or empty");
        }
        List<Post> posts = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> postsData = (List<Map<String, Object>>) extractedData.get("postsData");

        if (postsData == null) {
            return posts;
        }

        for (Map<String, Object> postData : postsData) {
            try {
                JsonNode postNode = (JsonNode) postData.get("postNode");
                Long sellerId = (Long) postData.get("sellerId");
                String pageId = (String) postData.get("pageId");
                Boolean isExisting = (Boolean) postData.get("isExisting");

                Post post = createPostFromNode(postNode, sellerId);
                List<PostChild> postChildren = createPostChildrenFromNode(postNode, pageId);

                post.setPostChildren(postChildren);
                post.setIsExisting(isExisting);
                posts.add(post);

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error transforming post: " + e.getMessage());
            }
        }
        return posts;
    }

    @Override
    @Transactional
    public List<Post> loadPost(ExtractorArgs args) {
        List<Post> posts = this.transformPost(args);
        List<ManagedPageCPL> managedPageCPLS = managedPageCPLRepository.findByIdSellerAndPlatform(args.getSeller().getId(), "facebook");

        // Fetch all existing Facebook posts once to avoid multiple database queries
        Map<String, List<PostChild>> existingPostsMap = new HashMap<>();
        List<PostChild> allExistingPosts = postChildRepository.findByIdSp(1L);
        List<Post> existingPosts = postRepository.findAll();
        // Group existing posts by platform_identifier
        for (PostChild postChild : allExistingPosts) {
            String platformIdentifier = postChild.getPlatformIdentifier();
            if (platformIdentifier != null) {
                existingPostsMap.computeIfAbsent(platformIdentifier, k -> new ArrayList<>()).add(postChild);
            }
        }

        for (Post post : posts) {
            if (post.getIsExisting() != null && post.getIsExisting()) {
                // Update existing post using pre-fetched data
                updateExistingPost(allExistingPosts,existingPosts,post, args.getSeller(), managedPageCPLS, existingPostsMap);
            } else {
                // Create new post
                createNewPost(post, args.getSeller(), managedPageCPLS);
            }
        }
        return posts;
    }
    
    private void createNewPost(Post post, Seller seller, List<ManagedPageCPL> managedPageCPLS) {
        postRepository.save(post);
        
        // First, save all post children to get their IDs
        for (PostChild postChild : post.getPostChildren()) {
            postChild.setIdPost(post.getId());
            postChildRepository.save(postChild);
        }
        
        // Then, set up parent-child relationships
        Integer mainPostChildId = null;
        for (PostChild postChild : post.getPostChildren()) {
            if ("main_post".equals(postChild.getType())) {
                mainPostChildId = postChild.getId();
                // Fetch and save reactions for the main post
                fetchAndSaveReactions(postChild, seller, managedPageCPLS);
            } else {
                // Set the main post as parent for all child posts
                if (mainPostChildId != null) {
                    postChild.setIdChild1(mainPostChildId);
                    postChildRepository.save(postChild);
                }
            }
        }
        
        System.out.println("Created new Facebook post: " + post.getId() + " with " + post.getPostChildren().size() + " children");
    }
    
    private void updateExistingPost(List<PostChild> existingPostChild,List<Post> existingPosts,Post post, Seller seller, List<ManagedPageCPL> managedPageCPLS, Map<String, List<PostChild>> existingPostsMap) {
        // Find the main_post child to get the platform_identifier (Facebook post ID)
        String facebookPostId = post.getPostChildren().stream()
            .filter(child -> "main_post".equals(child.getType()))
            .map(PostChild::getPlatformIdentifier)
            .findFirst()
            .orElse(null);
            
        if (facebookPostId == null) {
            System.err.println("Could not find main_post child with platform_identifier");
            return;
        }
        
        // Use pre-fetched data instead of database query
        List<PostChild> existingMainPost = existingPostsMap.get(facebookPostId);
        PostChild postM = null;
        if (existingMainPost == null || existingMainPost.isEmpty()) {
            System.err.println("Could not find existing post with platform_identifier: " + facebookPostId);
            return;
        }
        
        if (existingMainPost.size() == 2) {
            postM = existingMainPost.stream().filter(p -> p.getType().equals("main_post")).findFirst().orElse(null);
        } else if (existingMainPost.size() == 1) {
            postM = existingMainPost.get(0);
        } else {
            System.err.println("Could not find unique main_post child for platform_identifier: " + facebookPostId);
            return;
        }
        
        if (postM != null) {
            PostChild mainPostChild = postM;
            Integer existingPostId = mainPostChild.getIdPost();

            Optional<Post> existingPost = existingPosts.stream().filter(ep -> ep.getId().equals(existingPostId)).findFirst();
            if (existingPost.isPresent()) {
                Post postToUpdate = existingPost.get();
                postToUpdate.setCreateAt(post.getCreateAt());
                postRepository.save(postToUpdate);

                updatePostChildren(existingPostChild,post.getPostChildren(), existingPostId, seller, managedPageCPLS);
                
                System.out.println("Updated existing Facebook post: " + existingPostId + " (platform_identifier: " + facebookPostId + ")");
            }
        } else {
            System.err.println("Could not find existing post with platform_identifier: " + facebookPostId);
        }
    }
    
    private void updatePostChildren(List<PostChild> existingPostChilds,List<PostChild> newPostChildren, Integer postId, Seller seller, List<ManagedPageCPL> managedPageCPLS) {
        // Get existing post children for this post
        List<PostChild> existingChildren = existingPostChilds.stream()
            .filter(child -> child.getIdPost().equals(postId))
            .collect(Collectors.toList());
        
        // Find existing main post child
        PostChild existingMainPost = existingChildren.stream()
            .filter(child -> "main_post".equals(child.getType()))
            .findFirst()
            .orElse(null);
            
        if (existingMainPost == null) {
            System.err.println("Could not find existing main_post child for post: " + postId);
            return;
        }
        
        // Find new main post child
        PostChild newMainPost = newPostChildren.stream()
            .filter(child -> "main_post".equals(child.getType()))
            .findFirst()
            .orElse(null);
            
        if (newMainPost == null) {
            System.err.println("Could not find new main_post child");
            return;
        }
        
        boolean hasChanges = false;
        
        // Update main post child
        if (hasPostChildChanged(existingMainPost, newMainPost)) {
            System.out.println("Detected changes in Facebook main post: " + existingMainPost.getPlatformIdentifier());
            hasChanges = true;
            
            existingMainPost.setDescription(newMainPost.getDescription());
            existingMainPost.setPostUrl(newMainPost.getPostUrl());
            existingMainPost.setType(newMainPost.getType());
            existingMainPost.setMediaUrl(newMainPost.getMediaUrl());
            postChildRepository.save(existingMainPost);
        }
        
        // Fetch and save reactions for main post
        fetchAndSaveReactions(existingMainPost, seller, managedPageCPLS);
        
        // Handle child posts (photos, videos, etc.)
        List<PostChild> existingChildPosts = existingChildren.stream()
            .filter(child -> !"main_post".equals(child.getType()))
            .collect(Collectors.toList());
            
        List<PostChild> newChildPosts = newPostChildren.stream()
            .filter(child -> !"main_post".equals(child.getType()))
            .collect(Collectors.toList());
        
        // Delete existing child posts and create new ones
        // This is necessary because child posts don't have reliable platform_identifier for matching
        for (PostChild existingChild : existingChildPosts) {
            postChildRepository.delete(existingChild);
        }
        
        // Create new child posts
        for (PostChild newChild : newChildPosts) {
            newChild.setIdPost(postId);
            newChild.setIdChild1(existingMainPost.getId()); // Set parent-child relationship
            postChildRepository.save(newChild);
            hasChanges = true;
        }
        
        if (hasChanges) {
            System.out.println("Facebook post " + postId + " was updated with changes");
        } else {
            System.out.println("Facebook post " + postId + " has no changes");
        }
    }
    
    /**
     * Checks if a PostChild has changed compared to the existing one.
     */
    private boolean hasPostChildChanged(PostChild existing, PostChild newChild) {
        return !Objects.equals(existing.getDescription(), newChild.getDescription()) ||
               !Objects.equals(existing.getPostUrl(), newChild.getPostUrl()) ||
               !Objects.equals(existing.getType(), newChild.getType()) ||
               !Objects.equals(existing.getMediaUrl(), newChild.getMediaUrl());
    }idSeller

    private Post createPostFromNode(JsonNode postNode, Long sellerId) {
        Post post = new Post();

        // Set basic post properties
        post.setType("facebook_post");
        post.setIdSeller(sellerId);

        // Parse and set creation time
        String createdTimeStr = postNode.has("created_time") ? postNode.get("created_time").asText() : null;
        if (createdTimeStr != null) {
            try {
                LocalDateTime createdDateTime = Instant.parse(createdTimeStr)
                        .atZone(ZoneId.of("UTC")) // or ZoneId.systemDefault()
                        .toLocalDateTime();
                post.setCreateAt(createdDateTime);
            } catch (Exception e) {
                post.setCreateAt(LocalDateTime.now()); // Fallback to current time
            }
        } else {
            post.setCreateAt(LocalDateTime.now());
        }

        return post;
    }

    private List<PostChild> createPostChildrenFromNode(JsonNode postNode, String pageId) {
        List<PostChild> postChildren = new ArrayList<>();

        String postId = postNode.has("id") ? postNode.get("id").asText() : "";
        String message = postNode.has("message") ? postNode.get("message").asText() : "";

        // Create main post child with message
        PostChild mainChild = new PostChild();
        mainChild.setPostUrl(postNode.has("permalink_url") ? postNode.get("permalink_url").asText() : "");
        mainChild.setDescription(message);
        mainChild.setPlatformIdentifier(postId);
        mainChild.setType("main_post");
        mainChild.setIdSp(1L);

        postChildren.add(mainChild);

        // Process attachments
        if (postNode.has("attachments") && postNode.get("attachments").has("data")) {
            JsonNode attachmentsData = postNode.get("attachments").get("data");

            for (JsonNode attachment : attachmentsData) {
                processAttachment(attachment, postChildren, pageId, postId);
            }
        }

        return postChildren;
    }

    private void processAttachment(JsonNode attachment, List<PostChild> postChildren, String pageId, String postId) {
        String mediaType = attachment.has("media_type") ? attachment.get("media_type").asText() : "";
        String attachmentUrl = attachment.has("url") ? attachment.get("url").asText() : "";

        if ("album".equals(mediaType)) {
            if (attachment.has("subattachments") && attachment.get("subattachments").has("data")) {
                JsonNode subAttachments = attachment.get("subattachments").get("data");

                for (JsonNode subAttachment : subAttachments) {
                    PostChild photoChild = createPhotoChild(subAttachment, pageId, postId);
                    if (photoChild != null) {
                        postChildren.add(photoChild);
                    }
                }
            }
        } else if ("photo".equals(mediaType)) {
            PostChild photoChild = createSinglePhotoChild(attachment, pageId, postId);
            if (photoChild != null) {
                postChildren.add(photoChild);
            }
        } else {
            PostChild mediaChild = new PostChild();
            mediaChild.setPostUrl(attachmentUrl);
//            mediaChild.setPlatformIdentifier(postId);
            mediaChild.setType(mediaType);
            mediaChild.setIdSp(1L);

            if (attachment.has("media")) {
                JsonNode media = attachment.get("media");
                if (media.has("image") && media.get("image").has("src")) {
                    mediaChild.setMediaUrl(media.get("image").get("src").asText());
                }
            }
            if (attachment.has("id")) {
//                System.out.println("The id of the attachment is: " + attachment.get("id").asText());
                mediaChild.setPlatformIdentifier(attachment.get("id").asText());
            } else {
                mediaChild.setPlatformIdentifier(postId+"_bruh");
            }
            postChildren.add(mediaChild);
        }
    }

    private PostChild createPhotoChild(JsonNode subAttachment, String pageId, String postId) {
        if (!"photo".equals(subAttachment.get("type").asText())) {
            return null;
        }

        PostChild photoChild = new PostChild();

        if (subAttachment.has("url")) {
            photoChild.setPostUrl(subAttachment.get("url").asText());
        }

        if (subAttachment.has("media") && subAttachment.get("media").has("image")) {
            JsonNode image = subAttachment.get("media").get("image");
            if (image.has("src")) {
                photoChild.setMediaUrl(image.get("src").asText());
            }
        }

        String photoId = "";
        if (subAttachment.has("target") && subAttachment.get("target").has("id")) {}

        photoChild.setPlatformIdentifier(photoId.isEmpty() ? "" : photoId);
        photoChild.setType("photo");
        photoChild.setIdSp(1L);

        return photoChild;
    }

    private PostChild createSinglePhotoChild(JsonNode attachment, String pageId, String postId) {
        PostChild photoChild = new PostChild();

        // Set URLs
        if (attachment.has("url")) {
            photoChild.setPostUrl(attachment.get("url").asText());
        }

        if (attachment.has("media") && attachment.get("media").has("image")) {
            JsonNode image = attachment.get("media").get("image");
            if (image.has("src")) {
                photoChild.setMediaUrl(image.get("src").asText());
            }
        }

        photoChild.setPlatformIdentifier(postId);
        photoChild.setType("photo");
        photoChild.setIdSp(1L);

        return photoChild;
    }


    private void fetchAndSaveReactions(PostChild postChild, Seller seller,List<ManagedPageCPL> managedPageCPLS) {
        try {

            if (managedPageCPLS.isEmpty()) {
                return;
            }
            
            String accessToken = managedPageCPLS.get(0).getRefreshToken();
            System.out.println("Access Token: " + accessToken);
            String postId = postChild.getPlatformIdentifier();
            
            // Fetch reactions from Facebook API
            URI reactionsUri = UriComponentsBuilder.fromHttpUrl(FACEBOOK_API_BASE_URL)
                    .pathSegment(postId, "reactions")
                    .queryParam("fields", REACTIONS_FIELDS)
                    .queryParam("access_token", accessToken)
                    .build()
                    .toUri();
            System.out.println(reactionsUri.getQuery());
            String reactionsResponse = restTemplate.getForObject(reactionsUri, String.class);
            
            if (reactionsResponse != null) {
                JsonNode reactionsJson = objectMapper.readTree(reactionsResponse);
                JsonNode reactionsData = reactionsJson.get("data");
                
                if (reactionsData != null && reactionsData.isArray()) {
                    for (JsonNode reaction : reactionsData) {
                        String userId = reaction.get("id").asText();
                        String userName = reaction.get("name").asText();
                        String reactionType = reaction.get("type").asText();
//                        String createdTime = reaction.get("created_time").asText();

                        // Create likes history entry
                        LikesHistory likesHistory = new LikesHistory();
                        likesHistory.setIdChild(postChild.getId());
                        likesHistory.setReactions(1); // Each reaction counts as 1
                        likesHistory.setCreatedAt(LocalDateTime.now());
                        
                        likesHistoryRepository.save(likesHistory);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching reactions for post: " + postChild.getPlatformIdentifier());
        }
    }


    private LocalDateTime parseFacebookDateTime(String dateTimeStr) {
        try {
            return Instant.parse(dateTimeStr)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
