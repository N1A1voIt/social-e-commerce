package com.itu.socialcom.demo.analytics.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public  class HeatmapData {
    private List<String> xLabels;
    private List<String> yLabels;
    private List<HeatmapCell> cells;
    private String timeFrame;

    public HeatmapData(List<String> xLabels, List<String> yLabels,
                       List<HeatmapCell> cells, String timeFrame) {
        this.xLabels = xLabels;
        this.yLabels = yLabels;
        this.cells = cells;
        this.timeFrame = timeFrame;
    }

}
