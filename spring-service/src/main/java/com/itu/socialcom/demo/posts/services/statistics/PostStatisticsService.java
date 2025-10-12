package com.itu.socialcom.demo.posts.services.statistics;

import com.itu.socialcom.demo.posts.dto.PostStatisticsDto;
import com.itu.socialcom.demo.posts.repository.LikesHistoryRepository;
import com.itu.socialcom.demo.posts.repository.PostChildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostStatisticsService {

    @Autowired
    private LikesHistoryRepository likesHistoryRepository;
    
    @Autowired
    private PostChildRepository postChildRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PostStatisticsDto getPostStatistics(Integer postId) {
        // Get platform distribution
        List<PostStatisticsDto.PlatformReactionDistribution> platformReactions = getPlatformDistribution(postId);
        
        // Get time series data
        List<PostStatisticsDto.LikesTimeSeries> likesTimeSeries = getLikesTimeSeries(postId);
        
        // Calculate total likes
        int totalLikes = platformReactions.stream()
                .mapToInt(PostStatisticsDto.PlatformReactionDistribution::getLikesCount)
                .sum();

        return new PostStatisticsDto(
            platformReactions,
            likesTimeSeries,
            totalLikes,
            0, // totalViews - placeholder for future implementation
            0, // totalComments - placeholder for future implementation
            0  // totalShares - placeholder for future implementation
        );
    }

    private List<PostStatisticsDto.PlatformReactionDistribution> getPlatformDistribution(Integer postId) {
        List<Object[]> rawData = likesHistoryRepository.getPlatformDistribution(postId);
        List<PostStatisticsDto.PlatformReactionDistribution> platformReactions = new ArrayList<>();

        for (Object[] row : rawData) {
            Long platformId = (Long) row[0];
            Integer likesCount = (Integer) row[1];

            String platformName = PlatformNameMapper.getPlatformName(platformId.intValue());
            platformReactions.add(new PostStatisticsDto.PlatformReactionDistribution(
                platformName, 
                platformId, 
                likesCount, 
                0.0 // percentage will be calculated after we have all data
            ));
        }

        // Calculate percentages
        int totalLikes = platformReactions.stream()
                .mapToInt(PostStatisticsDto.PlatformReactionDistribution::getLikesCount)
                .sum();

        if (totalLikes > 0) {
            platformReactions.forEach(reaction -> {
                double percentage = (double) reaction.getLikesCount() / totalLikes * 100;
                reaction.setPercentage(percentage);
            });
        }

        return platformReactions;
    }

    private List<PostStatisticsDto.LikesTimeSeries> getLikesTimeSeries(Integer postId) {
        List<Object[]> rawData = likesHistoryRepository.getLikesTimeSeries(postId);
        List<PostStatisticsDto.LikesTimeSeries> likesTimeSeries = new ArrayList<>();

        for (Object[] row : rawData) {
            java.sql.Date date = (java.sql.Date) row[0];
            Long platformId = (Long) row[1];
            Integer likesCount = (Integer) row[2];

            String platformName = PlatformNameMapper.getPlatformName(platformId.intValue());
            likesTimeSeries.add(new PostStatisticsDto.LikesTimeSeries(
                date.toLocalDate().format(DATE_FORMATTER),
                likesCount,
                platformName,
                platformId
            ));
        }

        return likesTimeSeries;
    }
}
