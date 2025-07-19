package com.itu.socialcom.demo.socialmedia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Security configuration for OAuth endpoints.
 * Provides comprehensive security measures for OAuth flows.
 */
@Configuration
@EnableWebSecurity
public class OAuthSecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Value("${oauth.security.enabled:true}")
    private boolean securityEnabled;
    
    @Value("${oauth.cors.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private String[] allowedOrigins;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (!securityEnabled) {
            http.csrf().disable()
                .authorizeRequests().anyRequest().permitAll();
            return;
        }
        
        http
            // CSRF Protection - disabled for OAuth endpoints as they use state parameter
            .csrf()
                .ignoringAntMatchers("/api/oauth/**")
                .and()
            
            // CORS Configuration
            .cors()
                .and()
            
            // Security Headers
            .headers()
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true))
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .and()
            
            // OAuth endpoint security
            .authorizeRequests()
                .antMatchers("/api/oauth/*/authorize").authenticated()
                .antMatchers("/api/oauth/*/callback").permitAll()
                .antMatchers("/api/oauth/*/refresh").authenticated()
                .antMatchers("/api/page-management/**").authenticated()
                .anyRequest().permitAll()
                .and()
            
            // Session management
            .sessionManagement()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false);
    }
    
    /**
     * Redis template for rate limiting and state management.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}