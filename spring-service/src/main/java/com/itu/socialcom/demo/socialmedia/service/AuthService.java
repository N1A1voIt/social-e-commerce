package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.ManagedEntity;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AuthService {
    String exchangeForAccessToken(Map<String,String> params) throws IOException;
    String getLoginUrl();
    List<ManagedPageWithToken> getManagedPages() throws Exception;
    List<ManagedPage> savePages(String entityId,String userToken) throws Exception;
}
