package com.itu.socialcom.demo.socialmedia.dto;

import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;
import lombok.*;

@Getter
@Setter
public class ManagedPageWithToken {
    private ManagedPage managedPage;
    private String pageAccessToken;
    private String pageRefreshToken;
}
