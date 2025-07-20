package com.itu.socialcom.demo.socialmedia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itu.socialcom.demo.socialmedia.dto.ManagedPageWithToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class CacheV1 implements ManagedPageCachingSignature {
    @Autowired
    private StringRedisTemplate redisTemplate; // Auto-configured by Spring Boot
    @Autowired
    private ObjectMapper objectMapper; // For JSON serialization/deserialization
    private final Duration EXPIRATION_DURATION = Duration.ofMinutes(10); // Data expires after 10 minutes

    @Override
    public String cacheManagedPlatforms(List<ManagedPageWithToken> managedPages) throws Exception {
        String cacheKey = UUID.randomUUID().toString() + "-" + 
                          System.currentTimeMillis() + "-" + 
                          System.nanoTime() + "-" + 
                          Thread.currentThread().getId();
        String pagesJson = objectMapper.writeValueAsString(managedPages);
        redisTemplate.opsForValue().set(cacheKey, pagesJson, EXPIRATION_DURATION);
        return cacheKey;
    }
}
