package com.itu.socialcom.demo.posts.dto;

import com.itu.socialcom.demo.products.model.Product;
import lombok.Data;

import java.util.List;

@Data
public class SavePostArgs {
    List<PageDetails> pagesIds;
    List<MediaDetails> mediaDetails;
    String mainMessage;
    List<Long> idProducts;
    Long scheduledUnixTime; // optional, if provided we schedule instead of immediate publish
}