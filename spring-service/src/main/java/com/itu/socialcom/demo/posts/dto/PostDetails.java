package com.itu.socialcom.demo.posts.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostDetails {
    String pageId;
    String message;
    String pageAccessToken;
    List<String> mediaIds;
}
