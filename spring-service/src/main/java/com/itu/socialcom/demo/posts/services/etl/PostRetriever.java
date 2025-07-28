package com.itu.socialcom.demo.posts.services.etl;

import com.itu.socialcom.demo.posts.dto.ExtractorArgs;
import com.itu.socialcom.demo.posts.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostRetriever {
    @Autowired
    InstagramPostRetrieval instagramPostRetrieval;
    @Autowired
    FacebookPostRetrieval facebookPostRetrieval;

    @Transactional
    public List<Post> retrievePosts(ExtractorArgs args) throws Exception {
        List<Post> posts = new ArrayList<>();
        try {
            posts.addAll(facebookPostRetrieval.loadPost(args));
            posts.addAll(instagramPostRetrieval.loadPost(args));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return posts;
    }
}
