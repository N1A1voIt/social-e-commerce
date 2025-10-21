package com.itu.socialcom.demo.analytics.service;

import com.itu.socialcom.demo.analytics.dto.HeatmapCell;
import com.itu.socialcom.demo.analytics.dto.HeatmapData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
@Service
public class HeatmapService {
    @Autowired
    private EntityManager entityManager;
    public HeatmapData getHeatmapData(String timeFrame, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = buildHeatmapQuery(timeFrame);
        System.out.println(sql);
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return transformResults(results, timeFrame);
    }

    private String buildHeatmapQuery(String timeFrame) {
        String timeGrouping;
        String xAxisLabel;
        String yAxisLabel;

        timeGrouping = switch (timeFrame) {
            case "WEEKLY" ->
                // Days of week (rows) vs Hours (columns)
                    """
                            CASE EXTRACT(DOW FROM created_at)
                                WHEN 0 THEN 'Sunday'
                                WHEN 1 THEN 'Monday'
                                WHEN 2 THEN 'Tuesday'
                                WHEN 3 THEN 'Wednesday'
                                WHEN 4 THEN 'Thursday'
                                WHEN 5 THEN 'Friday'
                                WHEN 6 THEN 'Saturday'
                            END AS y_axis,
                            EXTRACT(HOUR FROM created_at) AS x_axis
                            """;
            case "MONTHLY" ->
                // Weeks (rows) vs Days of week (columns)
                    """
                            CONCAT('Week ', EXTRACT(WEEK FROM created_at)) AS y_axis,
                            CASE EXTRACT(DOW FROM created_at)
                                WHEN 0 THEN 'Sunday'
                                WHEN 1 THEN 'Monday'
                                WHEN 2 THEN 'Tuesday'
                                WHEN 3 THEN 'Wednesday'
                                WHEN 4 THEN 'Thursday'
                                WHEN 5 THEN 'Friday'
                                WHEN 6 THEN 'Saturday'
                            END AS x_axis
                            """;
            case "YEARLY" ->
                // Months (rows) vs Weeks in month (columns)
                    """
                            TO_CHAR(created_at, 'Month') AS y_axis,
                            CONCAT('Week ', CEIL(EXTRACT(DAY FROM created_at) / 7.0)) AS x_axis
                            """;
            default -> throw new IllegalArgumentException("Invalid timeframe: " + timeFrame);
        };

        return String.format("""
            SELECT
                %s,
                COUNT(*) AS post_count,
                ROUND(AVG(reactions), 2) AS avg_reactions,
                MIN(EXTRACT(DOW FROM created_at)) AS dow_order,
                MIN(EXTRACT(HOUR FROM created_at)) AS hour_order,
                MIN(EXTRACT(WEEK FROM created_at)) AS week_order,
                MIN(EXTRACT(MONTH FROM created_at)) AS month_order
            FROM likes_history
            WHERE reactions IS NOT NULL
                AND created_at >= :startDate
                AND created_at <= :endDate
            GROUP BY y_axis, x_axis
            """, timeGrouping);
//        ORDER BY
//        CASE
//        WHEN y_axis ~ '^[0-9]' THEN CAST(REGEXP_REPLACE(y_axis, '[^0-9]', '', 'g') AS INTEGER)
//        ELSE dow_order
//        END,
//                CASE
//        WHEN x_axis ~ '^[0-9]' THEN CAST(REGEXP_REPLACE(x_axis, '[^0-9]', '', 'g') AS INTEGER)
//        ELSE hour_order
//        END
    }

    private HeatmapData transformResults(List<Object[]> results, String timeFrame) {
        List<HeatmapCell> cells = new ArrayList<>();
        Set<String> xLabels = new LinkedHashSet<>();
        Set<String> yLabels = new LinkedHashSet<>();

        for (Object[] row : results) {
            String yAxis = (String) row[0];
            String xAxis = row[1] instanceof Number
                    ? String.valueOf(((Number) row[1]).intValue()) + ":00"
                    : (String) row[1];

            Long postCount = ((Number) row[2]).longValue();
            Double avgReactions = row[3] != null
                    ? ((Number) row[3]).doubleValue()
                    : 0.0;

            xLabels.add(xAxis);
            yLabels.add(yAxis);

            cells.add(new HeatmapCell(xAxis, yAxis, postCount, avgReactions));
        }

        return new HeatmapData(
                new ArrayList<>(xLabels),
                new ArrayList<>(yLabels),
                cells,
                timeFrame
        );
    }

}

