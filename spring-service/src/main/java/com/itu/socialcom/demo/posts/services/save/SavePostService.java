package com.itu.socialcom.demo.posts.services.save;

import com.itu.socialcom.demo.posts.dto.MediaDetails;
import com.itu.socialcom.demo.posts.dto.PostDetails;
import com.itu.socialcom.demo.posts.entity.Post;
import com.itu.socialcom.demo.posts.entity.PostChild;

import java.io.IOException;
import java.util.List;

public interface SavePostService {
    String uploadMediaUnpublished(MediaDetails mediaDetails) throws IOException;
    PostChild createPostWithMedia(PostDetails postDetails) throws IOException, InterruptedException;
    PostChild schedulePostWithMedia(PostDetails postDetails, long scheduledUnixTime) throws IOException;
}
