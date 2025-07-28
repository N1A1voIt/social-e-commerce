package com.itu.socialcom.demo.posts.services;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.dto.ExtractorArgs;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.repository.PostChildRepository;
import com.itu.socialcom.demo.posts.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public abstract class PostRetrievalSignature {
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostChildRepository postChildRepository;
    public Set<String> retrievePostIdentifiers(Long platformId) {
        List<PostChild> postChildren = postChildRepository.findByIdSp(platformId);
        Set<String> postIdentifiers = new HashSet<>();
        for (PostChild postChild : postChildren) {
            postIdentifiers.add(postChild.getPlatformIdentifier());
        }
        return postIdentifiers;
    }
    public abstract Map<String, Object> extractPostData(ExtractorArgs args);
    public abstract List<Post> transformPost(ExtractorArgs args);
    public abstract List<Post> loadPost(ExtractorArgs args);
}
