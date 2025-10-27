package com.itu.socialcom.demo.posts.dto;

import lombok.Data;

import java.util.List;

@Data
public class RepostArgs {
    private Integer originalPostId; // ID of the post to reuse
    private List<PageDetails> pagesIds; // Platforms/pages to share to
    private String additionalMessage; // Optional: additional message to append
    private Boolean includeOriginalMessage; // Whether to include original message
    private Long scheduledUnixTime; // Optional: schedule for later
}

