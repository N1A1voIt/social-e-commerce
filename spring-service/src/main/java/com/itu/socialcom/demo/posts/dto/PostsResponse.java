package com.itu.socialcom.demo.posts.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostsResponse {
    private List<MotherPostDisplay> posts;
    private int totalPosts;
}
