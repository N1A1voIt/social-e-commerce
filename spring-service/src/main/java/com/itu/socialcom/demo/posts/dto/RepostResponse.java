package com.itu.socialcom.demo.posts.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RepostResponse {
    private Integer newPostId;
    private Integer originalPostId;
    private List<PostChildResponse> publishedChildren;
    private String message;
    private LocalDateTime createdAt;
    private Boolean isScheduled;
}

