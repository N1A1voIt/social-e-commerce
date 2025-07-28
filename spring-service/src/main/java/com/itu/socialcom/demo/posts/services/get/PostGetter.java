package com.itu.socialcom.demo.posts.services.get;

import com.itu.socialcom.demo.posts.entity.PostChildMedia;
import com.itu.socialcom.demo.posts.repository.PostChildMediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostGetter {
    @Autowired
    PostChildMediaRepository postChildMediaRepository;
    List<PostChildMedia> getPostChildMedia() {
        List<PostChildMedia> postChildMediaList = postChildMediaRepository.findAll();

    }
}
