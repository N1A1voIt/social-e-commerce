package com.itu.socialcom.demo.posts.services.etl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.dto.ExtractorArgs;
import com.itu.socialcom.demo.posts.entity.Media;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.entity.VRefreshTokenHolder;
import com.itu.socialcom.demo.posts.repository.MediaRepository;
import com.itu.socialcom.demo.posts.repository.PostChildRepository;
import com.itu.socialcom.demo.posts.repository.VRefreshTokenHolderRepository;
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
                        .queryParam("fields", "id,caption,media_type,media_url,permalink,timestamp,children{media_type,media_url,timestamp}")
                        .queryParam("access_token", accessToken)
                        .build()
                        .toUri();

                String response = restTemplate.getForObject(uri, String.class);

                if (response != null) {
                    JsonNode jsonResponse = objectMapper.readTree(response);
                    JsonNode dataNode = jsonResponse.get("data");

                    if (dataNode != null && dataNode.isArray()) {
                        for (JsonNode postNode : dataNode) {
                            if (postIdentifiers.contains(postNode.get("id").asText())) continue;
                            Map<String, Object> postData = new HashMap<>();

                            postData.put("sellerId", seller.getId());
                            postData.put("instagramUserId", instagramUserId);
                            postData.put("postId", postNode.path("id").asText());
                            postData.put("caption", postNode.path("caption").asText(""));
                            postData.put("mediaType", postNode.path("media_type").asText());
                            postData.put("permalink", postNode.path("permalink").asText());
                            postData.put("timestamp", postNode.path("timestamp").asText());

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
    public List<Post> transformPost(ExtractorArgs args) {
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
        List<Post> posts = this.transformPost(args);
        if (posts != null && !posts.isEmpty()) {
            for (Post post : posts) {
                postRepository.save(post);
                for (PostChild child : post.getPostChildren()) {
                    child.setIdPost(post.getId());
                    postChildRepository.save(child);
                    for (Media media : child.getMediaList()) {
                        media.setIdChild(child.getId());
                        mediaRepository.save(media);
                    }
                }
            }
        }
        return posts;
    }
}
