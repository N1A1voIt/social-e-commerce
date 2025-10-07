package com.itu.socialcom.demo.posts.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostStatisticsDto {
    private List<PlatformReactionDistribution> platformReactions;
    private List<LikesTimeSeries> likesTimeSeries;
    private Integer totalLikes;
    private Integer totalViews;
    private Integer totalComments;
    private Integer totalShares;

    public PostStatisticsDto() {}

    public PostStatisticsDto(List<PlatformReactionDistribution> platformReactions, 
                           List<LikesTimeSeries> likesTimeSeries,
                           Integer totalLikes, Integer totalViews, 
                           Integer totalComments, Integer totalShares) {
        this.platformReactions = platformReactions;
        this.likesTimeSeries = likesTimeSeries;
        this.totalLikes = totalLikes;
        this.totalViews = totalViews;
        this.totalComments = totalComments;
        this.totalShares = totalShares;
    }

    @Getter
    @Setter
    public static class PlatformReactionDistribution {
        private String platformName;
        private Long platformId;
        private Integer likesCount;
        private Double percentage;

        public PlatformReactionDistribution() {}

        public PlatformReactionDistribution(String platformName, Long platformId, Integer likesCount, Double percentage) {
            this.platformName = platformName;
            this.platformId = platformId;
            this.likesCount = likesCount;
            this.percentage = percentage;
        }
    }

    @Getter
    @Setter
    public static class LikesTimeSeries {
        private String date;
        private Integer likesCount;
        private String platformName;
        private Long platformId;

        public LikesTimeSeries() {}

        public LikesTimeSeries(String date, Integer likesCount, String platformName, Long platformId) {
            this.date = date;
            this.likesCount = likesCount;
            this.platformName = platformName;
            this.platformId = platformId;
        }
    }
}

