package com.itu.socialcom.demo.posts.services.save;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.dto.*;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.repository.PostChildRepository;
import com.itu.socialcom.demo.posts.repository.PostRepository;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostFromPreviewSaver {
    @Autowired
    SaverFactory saverFactory;
    @Autowired
    ManagedPageCPLRepository managedPageCPLRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostChildRepository postChildRepository;

    @Transactional
    public List<PostChild> createPostsFromPreview(CreatePostFromPreviewArgs args, Seller seller) throws Exception {
        // Get all managed pages for the seller
        List<ManagedPageCPL> managedPageWithToken = managedPageCPLRepository.findByIdSeller(seller.getId());
        HashMap<String, ManagedPageCPL> managedPageWithTokenMap = new HashMap<>();
        for (ManagedPageCPL managedPageCPL : managedPageWithToken) {
            managedPageWithTokenMap.put(managedPageCPL.getPlatformIdentifier(), managedPageCPL);
        }

        // Group page details by platform
        Map<String, List<PageDetails>> pagesByPlatform = args.getPageDetails().stream()
                .collect(Collectors.groupingBy(PageDetails::getPlatform));

        // Create a map of platform previews for easy access
        Map<String, PlatformPreviewItem> previewsByPlatform = args.getPlatformPreviews().stream()
                .collect(Collectors.toMap(PlatformPreviewItem::getPlatform, item -> item));

        List<PostChild> posts = new ArrayList<>();

        // Iterate through each platform and create posts for all pages of that platform
        for (Map.Entry<String, List<PageDetails>> entry : pagesByPlatform.entrySet()) {
            String platform = entry.getKey();
            List<PageDetails> pagesForPlatform = entry.getValue();
            PlatformPreviewItem preview = previewsByPlatform.get(platform);

            if (preview == null) {
                System.err.println("No preview found for platform: " + platform);
                continue;
            }

            // Get the appropriate saver for this platform
            SavePostService savePostService = saverFactory.getSaver(platform);

            // Create posts for each page of this platform
            for (PageDetails pageDetail : pagesForPlatform) {
                try {
                    PostDetails postDetails = new PostDetails();
                    postDetails.setMessage(preview.getMainMessage());
                    postDetails.setPageId(pageDetail.getPageId());
                    
                    ManagedPageCPL managedPage = managedPageWithTokenMap.get(pageDetail.getPageId());
                    if (managedPage == null) {
                        System.err.println("No managed page found for pageId: " + pageDetail.getPageId());
                        continue;
                    }
                    postDetails.setPageAccessToken(managedPage.getRefreshToken());

                    // Upload all media for this page
                    List<String> mediaFbids = new ArrayList<>();
                    for (MediaDetailPreview mediaDetail : preview.getMediaDetails()) {
                        try {
                            MediaDetails mediaDetails = new MediaDetails();
                            mediaDetails.setImageUrl(mediaDetail.getImageUrl());
                            mediaDetails.setMessage(mediaDetail.getMessage());
                            mediaDetails.setPageAccessToken(postDetails.getPageAccessToken());
                            mediaDetails.setPageId(postDetails.getPageId());
                            
                            String mediaFbid = savePostService.uploadMediaUnpublished(mediaDetails);
                            mediaFbids.add(mediaFbid);
                        } catch (Exception e) {
                            System.err.println("Failed to upload media for page: " + pageDetail.getPageId());
                            e.printStackTrace();
                        }
                    }
                    
                    postDetails.setMediaIds(mediaFbids);

                    // Create the post (scheduled or immediate)
                    PostChild postChild;
                    if (args.getScheduledUnixTime() != null && args.getScheduledUnixTime() > 0) {
                        postChild = savePostService.schedulePostWithMedia(postDetails, args.getScheduledUnixTime());
                    } else {
                        postChild = savePostService.createPostWithMedia(postDetails);
                    }
                    
                    posts.add(postChild);
                } catch (Exception e) {
                    System.err.println("Failed to create post for page: " + pageDetail.getPageId());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Posts created: " + posts.size());

        // Save the mother post
        Post post = new Post();
        post.setType(args.getScheduledUnixTime() != null && args.getScheduledUnixTime() > 0 ? "scheduled_post" : "post");
        
        if (args.getScheduledUnixTime() != null && args.getScheduledUnixTime() > 0) {
            Instant instant = Instant.ofEpochMilli(args.getScheduledUnixTime());
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            post.setCreateAt(dateTime);
        } else {
            post.setCreateAt(LocalDateTime.now());
        }
        
        post.setIdSeller(seller.getId());
        post.setPostChildren(posts);
        postRepository.save(post);

        // Save all child posts with their relationships
        for (int i = 0; i < post.getPostChildren().size(); i++) {
            if (post.getPostChildren().get(i) == null) {
                continue;
            }
            post.getPostChildren().get(i).setIdPost(post.getId());
            postChildRepository.save(post.getPostChildren().get(i));
            
            // Save grandchildren if any
            if (post.getPostChildren().get(i).getPostChilds() != null) {
                for (int j = 0; j < post.getPostChildren().get(i).getPostChilds().size(); j++) {
                    post.getPostChildren().get(i).getPostChilds().get(j).setIdChild1(post.getPostChildren().get(i).getId());
                    post.getPostChildren().get(i).getPostChilds().get(j).setIdPost(post.getId());
                    postChildRepository.save(post.getPostChildren().get(i).getPostChilds().get(j));
                }
            }
        }

        return posts;
    }
}

