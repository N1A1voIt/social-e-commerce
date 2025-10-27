package com.itu.socialcom.demo.posts.services.repost;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.dto.*;
import com.itu.socialcom.demo.posts.entity.Media;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.repository.MediaRepository;
import com.itu.socialcom.demo.posts.repository.PostChildRepository;
import com.itu.socialcom.demo.posts.repository.PostRepository;
import com.itu.socialcom.demo.posts.services.save.SavePostService;
import com.itu.socialcom.demo.posts.services.save.SaverFactory;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import com.itu.socialcom.demo.storage.SupabaseStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class RepostService {
    private static final Logger log = LoggerFactory.getLogger(RepostService.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostChildRepository postChildRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private ManagedPageCPLRepository managedPageCPLRepository;

    @Autowired
    private SaverFactory saverFactory;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    /**
     * Repost/share an existing post to new platforms
     */
    @Transactional
    public RepostResponse repost(RepostArgs repostArgs, Seller seller) throws Exception {
        // 1. Validate the original post exists and belongs to the seller
        Post originalPost = postRepository.findById(repostArgs.getOriginalPostId())
                .orElseThrow(() -> new IllegalArgumentException("Original post not found"));

        if (!originalPost.getIdSeller().equals(seller.getId())) {
            throw new IllegalArgumentException("You don't have permission to repost this post");
        }

        // 2. Get original post children to extract content
        List<PostChild> originalChildren = postChildRepository.findByIdPost(originalPost.getId());
        if (originalChildren.isEmpty()) {
            throw new IllegalArgumentException("Original post has no content");
        }

        // 3. Extract message and media from the first child (or combine if needed)
        PostChild firstChild = originalChildren.get(0);
        String message = buildRepostMessage(firstChild.getDescription(), repostArgs);

        // 4. Get media from original post
        List<Media> originalMedia = mediaRepository.findByIdChild(firstChild.getId());

        // 5. Download media from platform URLs and re-upload to Supabase
        List<MediaDetails> mediaDetailsList = new ArrayList<>();
        for (Media media : originalMedia) {
            try {
                // Download from original URL (Facebook/Instagram/etc.) and upload to Supabase
                String supabaseUrl = supabaseStorageService.downloadAndUploadToSupabase(
                        media.getMediaUrl(),
                        null // Auto-generate filename
                );

                MediaDetails md = new MediaDetails();
                md.setImageUrl(supabaseUrl); // Use Supabase URL
                mediaDetailsList.add(md);

                log.info("Media migrated to Supabase: {} -> {}", media.getMediaUrl(), supabaseUrl);
            } catch (Exception e) {
                log.error("Failed to migrate media to Supabase: {}", e.getMessage(), e);
                // Fall back to original URL if Supabase upload fails
                MediaDetails md = new MediaDetails();
                md.setImageUrl(media.getMediaUrl());
                mediaDetailsList.add(md);
            }
        }

        // 6. Get managed pages and prepare for posting
        List<ManagedPageCPL> managedPages = managedPageCPLRepository.findByIdSeller(seller.getId());
        HashMap<String, ManagedPageCPL> pageMap = new HashMap<>();
        for (ManagedPageCPL page : managedPages) {
            pageMap.put(page.getPlatformIdentifier(), page);
        }

        // 7. Create new posts on selected platforms
        List<PostChild> newPostChildren = new ArrayList<>();
        List<PostChildResponse> responses = new ArrayList<>();

        for (PageDetails pageDetails : repostArgs.getPagesIds()) {
            try {
                SavePostService savePostService = saverFactory.getSaver(pageDetails.getPlatform());
                PostDetails postDetails = new PostDetails();
                postDetails.setMessage(message);
                postDetails.setPageId(pageDetails.getPageId());

                ManagedPageCPL managedPage = pageMap.get(postDetails.getPageId());
                if (managedPage == null) {
                    log.warn("Page {} not found in managed pages", postDetails.getPageId());
                    continue;
                }

                postDetails.setPageAccessToken(managedPage.getRefreshToken());

                // Upload media
                List<String> mediaIds = new ArrayList<>();
                for (MediaDetails mediaDetails : mediaDetailsList) {
                    try {
                        mediaDetails.setPageAccessToken(postDetails.getPageAccessToken());
                        mediaDetails.setPageId(postDetails.getPageId());
                        String mediaId = savePostService.uploadMediaUnpublished(mediaDetails);
                        mediaIds.add(mediaId);
                    } catch (Exception e) {
                        log.error("Failed to upload media: {}", e.getMessage(), e);
                    }
                }
                postDetails.setMediaIds(mediaIds);

                // Create post (immediate or scheduled)
                PostChild newChild;
                if (repostArgs.getScheduledUnixTime() != null && repostArgs.getScheduledUnixTime() > 0) {
                    newChild = savePostService.schedulePostWithMedia(postDetails, repostArgs.getScheduledUnixTime());
                } else {
                    newChild = savePostService.createPostWithMedia(postDetails);
                }

                newPostChildren.add(newChild);

                PostChildResponse response = new PostChildResponse();
                response.setChildId(newChild.getId());
                response.setPlatform(pageDetails.getPlatform());
                response.setPostUrl(newChild.getPostUrl());
                response.setSuccess(true);
                responses.add(response);

            } catch (Exception e) {
                log.error("Failed to repost to platform {}: {}", pageDetails.getPlatform(), e.getMessage(), e);
                PostChildResponse response = new PostChildResponse();
                response.setPlatform(pageDetails.getPlatform());
                response.setSuccess(false);
                response.setErrorMessage(e.getMessage());
                responses.add(response);
            }
        }

        // 8. Save the new post parent record
        Post newPost = new Post();
        newPost.setType(repostArgs.getScheduledUnixTime() != null ? "scheduled_repost" : "repost");

        LocalDateTime createTime;
        if (repostArgs.getScheduledUnixTime() != null && repostArgs.getScheduledUnixTime() > 0) {
            Instant instant = Instant.ofEpochMilli(repostArgs.getScheduledUnixTime());
            createTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } else {
            createTime = LocalDateTime.now();
        }

        newPost.setCreateAt(createTime);
        newPost.setIdSeller(seller.getId());
        newPost.setPostChildren(newPostChildren);
        postRepository.save(newPost);

        // 9. Save post children
        for (PostChild child : newPostChildren) {
            if (child != null) {
                child.setIdPost(newPost.getId());
                postChildRepository.save(child);

                // Save nested children if any
                if (child.getPostChilds() != null) {
                    for (PostChild nestedChild : child.getPostChilds()) {
                        nestedChild.setIdChild1(child.getId());
                        nestedChild.setIdPost(newPost.getId());
                        postChildRepository.save(nestedChild);
                    }
                }
            }
        }

        // 10. Build response
        RepostResponse repostResponse = new RepostResponse();
        repostResponse.setNewPostId(newPost.getId());
        repostResponse.setOriginalPostId(repostArgs.getOriginalPostId());
        repostResponse.setPublishedChildren(responses);
        repostResponse.setMessage("Post successfully reposted to " + responses.stream().filter(PostChildResponse::getSuccess).count() + " platform(s)");
        repostResponse.setCreatedAt(createTime);
        repostResponse.setIsScheduled(repostArgs.getScheduledUnixTime() != null && repostArgs.getScheduledUnixTime() > 0);

        return repostResponse;
    }

    /**
     * Build the message for reposting, optionally adding new content
     */
    private String buildRepostMessage(String originalMessage, RepostArgs args) {
        StringBuilder message = new StringBuilder();

        if (args.getIncludeOriginalMessage() == null || args.getIncludeOriginalMessage()) {
            if (originalMessage != null && !originalMessage.isEmpty()) {
                message.append(originalMessage);
            }
        }

        if (args.getAdditionalMessage() != null && !args.getAdditionalMessage().isEmpty()) {
            if (!message.isEmpty()) {
                message.append("\n\n");
            }
            message.append(args.getAdditionalMessage());
        }

        return message.toString();
    }

    /**
     * Get a preview of what will be reposted (without actually posting)
     */
    @Transactional(readOnly = true)
    public RepostPreview getRepostPreview(Integer postId, Seller seller) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getIdSeller().equals(seller.getId())) {
            throw new IllegalArgumentException("You don't have permission to view this post");
        }

        List<PostChild> children = postChildRepository.findByIdPost(post.getId());

        RepostPreview preview = new RepostPreview();
        preview.setPostId(post.getId());
        preview.setOriginalMessage(children.isEmpty() ? "" : children.get(0).getDescription());
        preview.setMediaCount(children.isEmpty() ? 0 : mediaRepository.findByIdChild(children.get(0).getId()).size());
        preview.setCreatedAt(post.getCreateAt());
        preview.setPostType(post.getType());

        return preview;
    }
}

