package com.itu.socialcom.demo.analytics.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DashboardStatsDto {
    private Double totalRevenue;
    private Double revenuePerUser;
    private Double bestDeal;
    private Long totalSales;
    private String dateRange;
    private PlatformRepartitionDto[] platformRepartition;
    private PagesRepartitionDto[] pagesRepartition;
    private SalesProgressionDto salesProgressionDto;
    private BestTimeToPost bestTimeToPost;
    private HeatmapData heatmapData;
    public DashboardStatsDto() {}
    
    public DashboardStatsDto(Double totalRevenue, Double revenuePerUser, Double bestDeal, Long totalSales, String dateRange) {
        this.totalRevenue = totalRevenue;
        this.revenuePerUser = revenuePerUser;
        this.bestDeal = bestDeal;
        this.totalSales = totalSales;
        this.dateRange = dateRange;
    }
}
