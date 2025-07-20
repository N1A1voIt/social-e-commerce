package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;

import java.util.List;

public interface ManagedPageCachingSignature {
    String cacheManagedPlatforms(List<ManagedPageWithToken> managedPages) throws Exception;
}
