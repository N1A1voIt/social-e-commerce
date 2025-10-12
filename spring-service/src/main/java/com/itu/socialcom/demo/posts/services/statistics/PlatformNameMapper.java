package com.itu.socialcom.demo.posts.services.statistics;

public class PlatformNameMapper {
    
    private static final int FACEBOOK_PLATFORM_ID = 1;
    private static final int INSTAGRAM_PLATFORM_ID = 2;
    
    private static final String FACEBOOK_NAME = "Facebook";
    private static final String INSTAGRAM_NAME = "Instagram";
    private static final String UNKNOWN_NAME = "Unknown";

    public static String getPlatformName(int platformId) {
        switch (platformId) {
            case FACEBOOK_PLATFORM_ID:
                return FACEBOOK_NAME;
            case INSTAGRAM_PLATFORM_ID:
                return INSTAGRAM_NAME;
            default:
                return UNKNOWN_NAME;
        }
    }
    
    public static boolean isFacebook(int platformId) {
        return platformId == FACEBOOK_PLATFORM_ID;
    }
    
    public static boolean isInstagram(int platformId) {
        return platformId == INSTAGRAM_PLATFORM_ID;
    }
}
