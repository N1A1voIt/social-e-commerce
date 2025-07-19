package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.ManagedEntity;

import java.util.List;

public interface AuthService {
    String exchangeForAccessToken();
    String getLoginUrl();
    List<ManagedEntity> getManagedEntities() throws Exception;
    String getEntityAccessToken(String entityId) throws Exception;
}
