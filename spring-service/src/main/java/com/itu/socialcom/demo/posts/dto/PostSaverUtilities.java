package com.itu.socialcom.demo.posts.dto;

import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PostSaverUtilities {
    List<ManagedPageCPL> managedPages;
    List<Product> products;
}
