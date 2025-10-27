package com.itu.socialcom.demo.posts.dto;

import lombok.Data;

@Data
public class PostChildResponse {
    private Integer childId;
    private String platform;
    private String postUrl;
    private Boolean success;
    private String errorMessage;
}

