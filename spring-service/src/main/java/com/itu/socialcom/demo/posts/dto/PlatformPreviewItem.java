package com.itu.socialcom.demo.posts.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlatformPreviewItem {
    String platform;
    String mainMessage;
    List<MediaDetailPreview> mediaDetails;
}

