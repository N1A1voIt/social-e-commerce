package com.itu.socialcom.demo.posts.dto;

import com.itu.socialcom.demo.posts.entity.Media;
import lombok.Data;

import java.util.List;

@Data
public class DisplayPost {
    Long id;
    String message;
    String platform;
    String username;
    List<Media> medias;
    List<DisplayPost> childPosts;
}
