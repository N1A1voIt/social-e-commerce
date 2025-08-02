package com.itu.socialcom.demo.posts.services.save;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.dto.MediaDetails;
import com.itu.socialcom.demo.posts.dto.PageDetails;
import com.itu.socialcom.demo.posts.dto.PostDetails;
import com.itu.socialcom.demo.posts.dto.SavePostArgs;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.repository.PostChildRepository;
import com.itu.socialcom.demo.posts.repository.PostRepository;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
@Service
public class GeneralPostSaver {
    @Autowired
    SaverFactory saverFactory;
    @Autowired
    ManagedPageCPLRepository managedPageCPLRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostChildRepository postChildRepository;

    @Transactional
    public List<PostChild> func(SavePostArgs savePostArgs, Seller seller) throws Exception {
        List<ManagedPageCPL> managedPageWithToken = managedPageCPLRepository.findByIdSeller(seller.getId());
        List<PostChild> posts = new ArrayList<>();
        HashMap<String, ManagedPageCPL> managedPageWithTokenMap = new HashMap<>();
        for (ManagedPageCPL managedPageCPL : managedPageWithToken) {
            managedPageWithTokenMap.put(managedPageCPL.getPlatformIdentifier(), managedPageCPL);
        }
        for (int i = 0; i < savePostArgs.getPagesIds().size(); i++) {
            PageDetails pageDetails = savePostArgs.getPagesIds().get(i);
            SavePostService savePostService = saverFactory.getSaver(pageDetails.getPlatform());
            PostDetails postDetails = new PostDetails();
            postDetails.setMessage(savePostArgs.getMainMessage());
            postDetails.setPageId(savePostArgs.getPagesIds().get(i).getPageId());
            System.out.println("Page ID: " + postDetails.getPageId());
            postDetails.setPageAccessToken(managedPageWithTokenMap.get(postDetails.getPageId()).getRefreshToken());
            List<String> mediaFbids = new ArrayList<>();
            for (MediaDetails mediaDetails : savePostArgs.getMediaDetails()) {
                try {
                    mediaDetails.setPageAccessToken(postDetails.getPageAccessToken());
                    mediaDetails.setPageId(postDetails.getPageId());
                    String mediaFbid = savePostService.uploadMediaUnpublished(mediaDetails);
                    mediaFbids.add(mediaFbid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            postDetails.setMediaIds(mediaFbids);
            posts.add(savePostService.createPostWithMedia(postDetails));
        }
        System.out.println("Posts created: " + posts.size());
        Post post = new Post();
        post.setType("post");
        post.setCreateAt(LocalDateTime.now());
        post.setIdSeller(seller.getId());
        post.setPostChildren(posts);
        postRepository.save(post);
        for (int i = 0; i < post.getPostChildren().size(); i++) {
            if (post.getPostChildren().get(i) == null) {continue;}
            post.getPostChildren().get(i).setIdPost(post.getId());
            postChildRepository.save(post.getPostChildren().get(i));
            for (int j = 0; j < post.getPostChildren().get(i).getPostChilds().size(); j++) {
                post.getPostChildren().get(i).getPostChilds().get(j).setIdChild1(post.getPostChildren().get(i).getId());
                post.getPostChildren().get(i).getPostChilds().get(j).setIdPost(post.getId());
                postChildRepository.save(post.getPostChildren().get(i).getPostChilds().get(j));
            }
        }
        return posts;
    }
}
