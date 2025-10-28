package com.itu.socialcom.demo.posts.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreatePostFromPreviewArgs {
    List<PlatformPreviewItem> platformPreviews;
    List<PageDetails> pageDetails;
    List<Long> idProducts;
    Long scheduledUnixTime; // optional, if provided we schedule instead of immediate publish
}

