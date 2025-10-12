package com.itu.socialcom.demo.posts.services.etl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.dto.ExtractorArgs;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.entity.VRefreshTokenHolder;
import com.itu.socialcom.demo.posts.repository.VRefreshTokenHolderRepository;
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

@Service
public class FacebookPostRetrieval extends PostRetrievalSignature{
    @Autowired
    VRefreshTokenHolderRepository vRefreshTokenHolderRepository;
//    private
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String FACEBOOK_API_BASE_URL = "https://graph.facebook.com/v23.0";
    private static final String POSTS_ENDPOINT = "/posts";
    private static final String FIELDS = "id,message,permalink_url,attachments{media_type,url,media,subattachments},created_time";

    @Override
    public Map<String, Object> extractPostData(ExtractorArgs args) {
        Seller seller = args.getSeller();
        Map<String, Object> extractedData = new HashMap<>();
        Set<String> postIdentifiers = postChildRepository.findDistinctPlatformIdentifierByIdSp(1L);
        List<Map<String, Object>> allPostsData = new ArrayList<>();

        List<VRefreshTokenHolder> vRefreshTokenHolders = vRefreshTokenHolderRepository
                .findByIdSellerAndIdSp(seller.getId().intValue(), 1L);

        for (VRefreshTokenHolder tokenHolder : vRefreshTokenHolders) {
            try {
                String pageId = tokenHolder.getPlatformIdentifier();
                String accessToken = tokenHolder.getToken();

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
                            if (postIdentifiers.contains(postNode.get("id").asText())) continue;
                            Map<String, Object> postData = new HashMap<>();
                            postData.put("pageId", pageId);
                            postData.put("postNode", postNode);
                            postData.put("sellerId", seller.getId());
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

                Post post = createPostFromNode(postNode, sellerId);
                List<PostChild> postChildren = createPostChildrenFromNode(postNode, pageId);

                post.setPostChildren(postChildren);
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
        for (Post post : posts) {
            postRepository.save(post);
            int postMereId = -1;
            for (PostChild postChild : post.getPostChildren()) {
                if (postMereId != -1) {
                    postChild.setIdChild1(postMereId);
                }
                postChild.setIdPost(post.getId());
                postChildRepository.save(postChild);
                if (postChild.getType().equals("main_post")) {
                    postMereId = postChild.getId();
                }
            }
        }
        return posts;
    }

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
            // Handle album with multiple photos
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
            // Handle single photo
            PostChild photoChild = createSinglePhotoChild(attachment, pageId, postId);
            if (photoChild != null) {
                postChildren.add(photoChild);
            }
        } else {
            // Handle other media types
            PostChild mediaChild = new PostChild();
            mediaChild.setPostUrl(attachmentUrl);
            mediaChild.setPlatformIdentifier(postId);
            mediaChild.setType(mediaType);
            mediaChild.setIdSp(1L);

            if (attachment.has("media")) {
                JsonNode media = attachment.get("media");
                if (media.has("image") && media.get("image").has("src")) {
                    mediaChild.setMediaUrl(media.get("image").get("src").asText());
                }
            }

            postChildren.add(mediaChild);
        }
    }

    private PostChild createPhotoChild(JsonNode subAttachment, String pageId, String postId) {
        if (!"photo".equals(subAttachment.get("type").asText())) {
            return null;
        }

        PostChild photoChild = new PostChild();

        // Set URLs
        if (subAttachment.has("url")) {
            photoChild.setPostUrl(subAttachment.get("url").asText());
        }

        if (subAttachment.has("media") && subAttachment.get("media").has("image")) {
            JsonNode image = subAttachment.get("media").get("image");
            if (image.has("src")) {
                photoChild.setMediaUrl(image.get("src").asText());
            }
        }

        // Set identifiers
        String photoId = "";
        if (subAttachment.has("target") && subAttachment.get("target").has("id")) {
            photoId = subAttachment.get("target").get("id").asText();
        }

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
}
