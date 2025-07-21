package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.ManagedEntity;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;

import java.io.IOException;
import java.util.List;

public interface AuthService {
    String exchangeForAccessToken(String code) throws IOException;
    String getLoginUrl();
    List<ManagedPageWithToken> getManagedPages() throws Exception;
    List<ManagedPage> savePages(String entityId,String userToken) throws Exception;
}
