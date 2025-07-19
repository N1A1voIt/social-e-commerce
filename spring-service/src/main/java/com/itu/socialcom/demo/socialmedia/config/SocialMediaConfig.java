package com.itu.socialcom.demo.socialmedia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class to enable OAuth configuration properties
 * and set up social media integration components.
 */
@Configuration
@EnableConfigurationProperties(OAuthConfig.class)
public class SocialMediaConfig {
    
    /**
     * RestTemplate bean for making HTTP requests to OAuth providers
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * ObjectMapper bean for JSON parsing
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}