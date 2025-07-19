package com.itu.socialcom.demo.socialmedia.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable OAuth configuration properties
 * and set up social media integration components.
 */
@Configuration
@EnableConfigurationProperties(OAuthConfig.class)
public class SocialMediaConfig {
    
    // This class enables the OAuthConfig configuration properties
    // Additional beans and configuration can be added here as needed
}