package com.itu.socialcom.demo.posts.services.etl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.dto.ExtractorArgs;
import com.itu.socialcom.demo.posts.entity.Media;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.entity.LikesHistory;
import com.itu.socialcom.demo.posts.entity.VRefreshTokenHolder;
import com.itu.socialcom.demo.posts.repository.LikesHistoryRepository;
import com.itu.socialcom.demo.potentialCustomers.repository.PotentialCustomerV2Repository;
import com.itu.socialcom.demo.posts.repository.MediaRepository;
import com.itu.socialcom.demo.posts.repository.PostChildRepository;
import com.itu.socialcom.demo.posts.repository.VRefreshTokenHolderRepository;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Objects;
import java.util.Optional;

@Service
public class InstagramPostRetrieval extends PostRetrievalSignature{
    @Autowired
    VRefreshTokenHolderRepository vRefreshTokenHolderRepository;
    //    private
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    PostChildRepository postChildRepository;
    @Autowired
    ManagedPageCPLRepository managedPageCPLRepository;


    @Override
    public Map<String, Object> extractPostData(ExtractorArgs args) {
        Seller seller = args.getSeller();
        Set<String> postIdentifiers = postChildRepository.findDistinctPlatformIdentifierByIdSp(2L);
        Map<String, Object> extractedData = new HashMap<>();
        List<Map<String, Object>> allPostsData = new ArrayList<>();

        List<VRefreshTokenHolder> vRefreshTokenHolders = vRefreshTokenHolderRepository
                .findByIdSellerAndIdSp(seller.getId().intValue(), 2L);

        for (VRefreshTokenHolder tokenHolder : vRefreshTokenHolders) {
            try {
                String instagramUserId = tokenHolder.getPlatformIdentifier();
                String accessToken = tokenHolder.getToken();

                URI uri = UriComponentsBuilder
                        .fromHttpUrl("https://graph.facebook.com/v23.0")
                        .pathSegment(instagramUserId, "media")
                        .queryParam("fields", "id,caption,media_type,media_url,permalink,timestamp,children{media_type,media_url,timestamp},like_count")
                        .queryParam("access_token", accessToken)
                        .build()
                        .toUri();

                String response = restTemplate.getForObject(uri, String.class);

                if (response != null) {
                    JsonNode jsonResponse = objectMapper.readTree(response);
                    JsonNode dataNode = jsonResponse.get("data");

                    if (dataNode != null && dataNode.isArray()) {
                        for (JsonNode postNode : dataNode) {
                            String instagramPostId = postNode.get("id").asText();
                            Map<String, Object> postData = new HashMap<>();

                            postData.put("sellerId", seller.getId());
                            postData.put("instagramUserId", instagramUserId);
                            postData.put("postId", instagramPostId);
                            postData.put("caption", postNode.path("caption").asText(""));
                            postData.put("mediaType", postNode.path("media_type").asText());
                            postData.put("permalink", postNode.path("permalink").asText());
                            postData.put("timestamp", postNode.path("timestamp").asText());
                            postData.put("isExisting", postIdentifiers.contains(instagramPostId));

                            // Single media (image/video)
                            if (!postNode.has("children")) {
                                postData.put("mediaUrl", postNode.path("media_url").asText());
                            } else {
                                // Carousel
                                List<Map<String, Object>> carouselMedia = new ArrayList<>();
                                JsonNode children = postNode.path("children").path("data");
                                if (children.isArray()) {
                                    for (JsonNode child : children) {
                                        Map<String, Object> media = new HashMap<>();
                                        media.put("mediaUrl", child.path("media_url").asText());
                                        media.put("mediaType", child.path("media_type").asText());
                                        media.put("timestamp", child.path("timestamp").asText());
                                        carouselMedia.add(media);
                                    }
                                }
                                postData.put("carouselMedia", carouselMedia);
                            }

                            allPostsData.add(postData);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error extracting Instagram posts: " + e.getMessage());
            }
        }

        extractedData.put("instagramPosts", allPostsData);
        return extractedData;
    }


    @Override
    public List<Post> transformPost(ExtractorArgs args,HashMap<String, ManagedPageCPL> managedPageCPLHashMap) {
        List<Post> posts = new ArrayList<>();

        Map<String, Object> rawData = extractPostData(args);
        List<Map<String, Object>> instagramPosts = (List<Map<String, Object>>) rawData.get("instagramPosts");

        for (Map<String, Object> rawPost : instagramPosts) {
            Post post = new Post();
            post.setIdSeller(args.getSeller().getId());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

            ZonedDateTime zonedDateTime = ZonedDateTime.parse((String) rawPost.get("timestamp"), formatter);
            LocalDateTime createdDateTime = zonedDateTime.toLocalDateTime();

            post.setCreateAt(createdDateTime);
            post.setType("instagram_post");
            post.setIsExisting((Boolean) rawPost.get("isExisting"));

            PostChild postChild = new PostChild();
            postChild.setIdSp(2L);
            postChild.setPlatformIdentifier((String) rawPost.get("postId"));
            postChild.setPostUrl((String) rawPost.get("permalink"));
            postChild.setDescription((String) rawPost.get("caption"));
            postChild.setType((String) rawPost.get("mediaType"));

            if ("CAROUSEL_ALBUM".equalsIgnoreCase((String) rawPost.get("mediaType"))) {
                List<Map<String, Object>> carousel = (List<Map<String, Object>>) rawPost.get("carouselMedia");
                List<Media> mediaList = new ArrayList<>();
                for (Map<String, Object> mediaItem : carousel) {
                    Media media = new Media();
                    media.setMediaUrl((String) mediaItem.get("mediaUrl"));
                    media.setIdChild(0);
                    mediaList.add(media);
                }
                postChild.setMediaList(mediaList);
            } else {
                Media media = new Media();
                media.setMediaUrl((String) rawPost.get("mediaUrl"));
                media.setIdChild(0);
                postChild.setMediaList(List.of(media));
            }

            post.setPostChildren(List.of(postChild));
            posts.add(post);
        }

        return posts;
    }


    @Override
    @Transactional
    public List<Post> loadPost(ExtractorArgs args) {
        List<ManagedPageCPL> managedPageCPLS = managedPageCPLRepository.findByIdSellerAndPlatform(args.getSeller().getId(), "facebook");
        HashMap<String,ManagedPageCPL> managedPageCPLHashMap = new HashMap<>();
        for (int i = 0; i < managedPageCPLS.size(); i++) {
            managedPageCPLHashMap.put(managedPageCPLS.get(i).getPlatformIdentifier(),managedPageCPLS.get(i));
        }
        List<Post> posts = this.transformPost(args,managedPageCPLHashMap);
        if (posts != null && !posts.isEmpty()) {
            List<PostChild> postChildren = postChildRepository.findByIdSp(2L);
            List<Post> existingPosts = postRepository.findAll();
            for (Post post : posts) {
                if (post.getIsExisting() != null && post.getIsExisting()) {
                    // Update existing Instagram post
                    updateExistingInstagramPost(existingPosts,post,postChildren);
                } else {
                    // Create new Instagram post
                    createNewInstagramPost(post);
                }
            }
        }
        return posts;
    }
    
    private void createNewInstagramPost(Post post) {
        postRepository.save(post);
        for (PostChild child : post.getPostChildren()) {
            child.setIdPost(post.getId());
            postChildRepository.save(child);
            for (Media media : child.getMediaList()) {
                media.setIdChild(child.getId());
                mediaRepository.save(media);
            }
        }
        System.out.println("Created new Instagram post: " + post.getId());
    }
    
    private void updateExistingInstagramPost(List<Post> existingPosts,Post post,List<PostChild> postChildren) {
        // Find existing post by platform_identifier (Instagram post ID) and id_sp (2 for Instagram)
        String instagramPostId = post.getPostChildren().get(0).getPlatformIdentifier();

        PostChild existingPostChild = null;
        for (PostChild pc: postChildren) {
            if (instagramPostId.equals(pc.getPlatformIdentifier())) existingPostChild = pc;
        }
        if (existingPostChild != null) {
            PostChild mainPostChild = existingPostChild;
            Integer existingPostId = mainPostChild.getIdPost();
            
            // Update the main post
            Optional<Post> existingPost = existingPosts.stream().filter(ep -> ep.getId() == (existingPostId.longValue())).findFirst();
            if (existingPost.isPresent()) {
                Post postToUpdate = existingPost.get();
                postToUpdate.setCreateAt(post.getCreateAt());
                postRepository.save(postToUpdate);

                // Update post child
                PostChild newChild = post.getPostChildren().get(0);
                if (hasInstagramPostChanged(mainPostChild, newChild)) {
                    System.out.println("Detected changes in Instagram post: " + instagramPostId);

                    mainPostChild.setDescription(newChild.getDescription());
                    mainPostChild.setPostUrl(newChild.getPostUrl());
                    mainPostChild.setType(newChild.getType());
                    postChildRepository.save(mainPostChild);

                    // Update media
                    updateInstagramMedia(mainPostChild, newChild);
                }

                System.out.println("Updated existing Instagram post: " + existingPostId + " (platform_identifier: " + instagramPostId + ")");
            }
        } else {
            System.err.println("Could not find existing Instagram post with platform_identifier: " + instagramPostId);
        }
    }
    
    private void updateInstagramMedia(PostChild existingChild, PostChild newChild) {
        // Delete existing media
        List<Media> existingMedia = mediaRepository.findByIdChild(existingChild.getId());
        for (Media media : existingMedia) {
            mediaRepository.delete(media);
        }
        
        // Add new media
        for (Media media : newChild.getMediaList()) {
            media.setIdChild(existingChild.getId());
            mediaRepository.save(media);
        }
    }
    
    private boolean hasInstagramPostChanged(PostChild existing, PostChild newChild) {
        return !Objects.equals(existing.getDescription(), newChild.getDescription()) ||
               !Objects.equals(existing.getPostUrl(), newChild.getPostUrl()) ||
               !Objects.equals(existing.getType(), newChild.getType());
    }
}
