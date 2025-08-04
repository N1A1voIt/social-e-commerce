package com.itu.socialcom.demo.posts.services.save;

import com.itu.socialcom.demo.posts.dto.MediaDetails;
import com.itu.socialcom.demo.posts.dto.PostDetails;
import com.itu.socialcom.demo.posts.entity.PostChild;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class InstagramPostSaver implements SavePostService{
    @Override
    public String uploadMediaUnpublished(MediaDetails mediaDetails) throws IOException {
        return "";
    }

    @Override
    public PostChild createPostWithMedia(PostDetails postDetails) throws IOException {

        return null;
    }
}
