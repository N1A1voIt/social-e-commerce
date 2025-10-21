package com.itu.socialcom.demo.analytics.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeatmapCell {
    private String x;
    private String y;
    private Long postCount;
    private Double avgReactions;

    public HeatmapCell(String x, String y, Long postCount, Double avgReactions) {
        this.x = x;
        this.y = y;
        this.postCount = postCount;
        this.avgReactions = avgReactions;
    }
}
