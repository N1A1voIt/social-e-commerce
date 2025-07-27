package com.itu.socialcom.demo.posts.services;

import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.repository.PostChildRepository;
import com.itu.socialcom.demo.posts.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}
