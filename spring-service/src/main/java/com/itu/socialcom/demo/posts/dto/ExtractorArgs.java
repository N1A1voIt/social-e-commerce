package com.itu.socialcom.demo.posts.dto;

import com.itu.socialcom.demo.authentication.user.Seller;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Data
public class ExtractorArgs {
    Seller seller;
    Set<String> facebookIdentifiers;
    Set<String> instagramIdentifiers;
}
