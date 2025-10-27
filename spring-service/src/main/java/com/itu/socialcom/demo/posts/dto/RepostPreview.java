package com.itu.socialcom.demo.posts.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepostPreview {
    private Integer postId;
    private String originalMessage;
    private Integer mediaCount;
    private LocalDateTime createdAt;
    private String postType;
}

