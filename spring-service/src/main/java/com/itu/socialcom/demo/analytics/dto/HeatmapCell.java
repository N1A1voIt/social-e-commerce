package com.itu.socialcom.demo.analytics.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeatmapCell {
    private String x;
    private String y;
    private Long likeCount;  // Changed from postCount - this tracks the number of likes
    private Double avgReactions;  // Average reaction value per like

    public HeatmapCell(String x, String y, Long likeCount, Double avgReactions) {
        this.x = x;
        this.y = y;
        this.likeCount = likeCount;
        this.avgReactions = avgReactions;
    }
}
