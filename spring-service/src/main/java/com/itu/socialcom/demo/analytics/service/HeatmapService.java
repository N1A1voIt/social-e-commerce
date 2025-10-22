package com.itu.socialcom.demo.analytics.service;

import com.itu.socialcom.demo.analytics.dto.HeatmapCell;
import com.itu.socialcom.demo.analytics.dto.HeatmapData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class HeatmapService {

    @Autowired
    private EntityManager entityManager;

    // --- STATIC LABEL DEFINITIONS ---
    private static final String[] DAYS_OF_WEEK = {
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };

    // For MONTHLY view (Y-axis)
    private static final String[] WEEKS_OF_MONTH = {
            "Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6"
    };

    // For WEEKLY view (X-axis)
    private static final String[] HOURS_OF_DAY = IntStream.range(0, 24)
            .mapToObj(i -> i + ":00")
            .toArray(String[]::new);

    // For YEARLY view (X-axis)
    private static final String[] WEEKS_OF_YEAR = IntStream.rangeClosed(1, 53)
            .mapToObj(i -> "W" + i)
            .toArray(String[]::new);


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

    // --- REVISED buildHeatmapQuery ---
    private String buildHeatmapQuery(String timeFrame) {
        String timeGrouping;

        timeGrouping = switch (timeFrame) {
            case "WEEKLY" ->
                // Y-axis: Day of Week, X-axis: Hour of Day
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
                            EXTRACT(HOUR FROM created_at) AS x_axis_num,
                            CAST(EXTRACT(HOUR FROM created_at) AS VARCHAR) AS x_axis_str
                            """;
            case "MONTHLY" ->
                // Y-axis: Week of Month, X-axis: Day of Week
                    """
                            CONCAT('Week ', CEIL(EXTRACT(DAY FROM created_at) / 7.0)) AS y_axis,
                            0 AS x_axis_num,
                            CASE EXTRACT(DOW FROM created_at)
                                WHEN 0 THEN 'Sunday'
                                WHEN 1 THEN 'Monday'
                                WHEN 2 THEN 'Tuesday'
                                WHEN 3 THEN 'Wednesday'
                                WHEN 4 THEN 'Thursday'
                                WHEN 5 THEN 'Friday'
                                WHEN 6 THEN 'Saturday'
                            END AS x_axis_str
                            """;
            case "YEARLY" ->
                // Y-axis: Day of Week, X-axis: Week of Year (GITHUB STYLE)
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
                            EXTRACT(WEEK FROM created_at) AS x_axis_num,
                            CONCAT('W', EXTRACT(WEEK FROM created_at)) AS x_axis_str
                            """;
            default -> throw new IllegalArgumentException("Invalid timeframe: " + timeFrame);
        };

        return String.format("""
            SELECT
                y_axis,
                x_axis_str,
                COUNT(*) AS post_count,
                ROUND(AVG(reactions), 2) AS avg_reactions,
                -- Ordering columns
                MIN(EXTRACT(DOW FROM created_at)) AS y_order,
                MIN(x_axis_num) AS x_order
            FROM (
                SELECT
                    created_at,
                    reactions,
                    %s
                FROM likes_history
                WHERE reactions IS NOT NULL
                    AND created_at >= :startDate
                    AND created_at <= :endDate
            ) AS grouped_data
            GROUP BY y_axis, x_axis_str
            ORDER BY y_order, x_order
            """, timeGrouping);
    }

    // --- REVISED transformResults ---
    private HeatmapData transformResults(List<Object[]> results, String timeFrame) {
        Map<String, HeatmapCell> cellMap = new LinkedHashMap<>();
        List<String> xLabels;
        List<String> yLabels;

        // 1. Define full label sets and pre-populate the map with 0-value cells
        switch (timeFrame) {
            case "WEEKLY": // y=DayName, x=Hour
                yLabels = Arrays.asList(DAYS_OF_WEEK);
                xLabels = Arrays.asList(HOURS_OF_DAY);
                break;
            case "MONTHLY": // y=WeekOfMonth, x=DayName
                yLabels = Arrays.asList(WEEKS_OF_MONTH);
                xLabels = Arrays.asList(DAYS_OF_WEEK);
                break;
            case "YEARLY":
                yLabels = Arrays.asList(DAYS_OF_WEEK);
                xLabels = Arrays.asList(WEEKS_OF_YEAR);
                break;
            default:
                throw new IllegalArgumentException("Invalid timeframe: " + timeFrame);
        }

        // Pre-populate the map with 0-value cells
        for (String y : yLabels) {
            for (String x : xLabels) {
                cellMap.put(y + ":" + x, new HeatmapCell(x, y, 0L, 0.0));
            }
        }

        // 2. Iterate over actual SQL results and *update* the map
        for (Object[] row : results) {
            String yAxis = (String) row[0];
            String xAxis = (String) row[1];

            // For WEEKLY, format the numeric hour to "H:00"
            if (timeFrame.equals("WEEKLY")) {
                xAxis = xAxis + ":00";
            }

            Long postCount = ((Number) row[2]).longValue();
            Double avgReactions = row[3] != null
                    ? ((Number) row[3]).doubleValue()
                    : 0.0;

            String key = yAxis + ":" + xAxis;

            // Update the existing cell in the map
            if (cellMap.containsKey(key)) {
                cellMap.put(key, new HeatmapCell(xAxis, yAxis, postCount, avgReactions));
            }
            // else: data is outside our pre-defined grid, so we ignore it.
        }

        // 3. For YEARLY, we only want to show weeks that are in the map.
        // (Optional: If you want to *always* show 53 weeks, skip this block)
        if (timeFrame.equals("YEARLY")) {
            // Filter xLabels to only those present in the query range
            // This avoids showing 53 weeks if you only selected a 3-month range.
            Set<String> presentXLabels = new LinkedHashSet<>();
            for (Object[] row : results) {
                presentXLabels.add((String) row[1]); // "W1", "W2", etc.
            }
            if (!presentXLabels.isEmpty()) {
                xLabels = new ArrayList<>(presentXLabels);
                // Re-filter the cellMap to only include these labels
                Map<String, HeatmapCell> filteredCellMap = new LinkedHashMap<>();
                for (String y : yLabels) {
                    for (String x : xLabels) {
                        filteredCellMap.put(y + ":" + x, cellMap.get(y + ":" + x));
                    }
                }
                cellMap = filteredCellMap;
            }
        }

        return new HeatmapData(
                xLabels,
                yLabels,
                new ArrayList<>(cellMap.values()),
                timeFrame
        );
    }
}