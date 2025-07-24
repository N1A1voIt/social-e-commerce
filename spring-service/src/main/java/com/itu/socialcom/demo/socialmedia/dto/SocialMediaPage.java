package com.itu.socialcom.demo.socialmedia.dto;

import java.util.List;

/**
 * DTO representing a social media page/account from external platforms
 */
public class SocialMediaPage {
    
    private String id;
    private String name;
    private String category;
    private String profilePictureUrl;
    private String accessToken;
    private List<String> permissions;
    private String platform;
    
    public SocialMediaPage() {}
    
    public SocialMediaPage(String id, String name, String category, String profilePictureUrl, 
                          String accessToken, List<String> permissions, String platform) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.profilePictureUrl = profilePictureUrl;
        this.accessToken = accessToken;
        this.permissions = permissions;
        this.platform = platform;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public List<String> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    @Override
    public String toString() {
        return "SocialMediaPage{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                ", accessToken='" + (accessToken != null ? "[REDACTED]" : null) + '\'' +
                ", permissions=" + permissions +
                ", platform='" + platform + '\'' +
                '}';
    }
}