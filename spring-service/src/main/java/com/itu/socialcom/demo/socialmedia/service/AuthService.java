package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.ManagedEntity;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;

import java.util.List;

public interface AuthService {
    String exchangeForAccessToken(String code);
    String getLoginUrl();
    List<ManagedPageWithToken> getManagedPages() throws Exception;
    String getEntityAccessToken(String entityId) throws Exception;
}
