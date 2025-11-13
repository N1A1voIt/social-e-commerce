package com.itu.socialcom.demo.analytics.service;

import com.itu.socialcom.demo.analytics.dto.HeatmapCell;
import com.itu.socialcom.demo.analytics.dto.HeatmapData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
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

        return transformResults(results, timeFrame, startDate, endDate);
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
                // Y-axis: Day of Week, X-axis: Date (GITHUB STYLE)
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
                            0 AS x_axis_num,
                            TO_CHAR(DATE(created_at), 'YYYY-MM-DD') AS x_axis_str
                            """;
            default -> throw new IllegalArgumentException("Invalid timeframe: " + timeFrame);
        };

        return String.format("""
            SELECT
                y_axis,
                x_axis_str,
                COUNT(*) AS like_count,
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
    private HeatmapData transformResults(List<Object[]> results, String timeFrame, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, HeatmapCell> cellMap = new LinkedHashMap<>();
        List<String> xLabels;
        List<String> yLabels;

        // 1. Define full label sets and pre-populate the map with 0-value cells
        switch (timeFrame) {
            case "WEEKLY": // y=DayName, x=Hour
                yLabels = Arrays.asList(DAYS_OF_WEEK);
                xLabels = Arrays.asList(HOURS_OF_DAY);
                
                // Pre-populate the map with 0-value cells
                for (String y : yLabels) {
                    for (String x : xLabels) {
                        cellMap.put(y + ":" + x, new HeatmapCell(x, y, 0L, 0.0));
                    }
                }
                
                // Update with actual data
                for (Object[] row : results) {
                    String yAxis = (String) row[0];
                    String xAxis = row[1] + ":00";
                    Long likeCount = ((Number) row[2]).longValue();
                    Double avgReactions = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
                    String key = yAxis + ":" + xAxis;
                    if (cellMap.containsKey(key)) {
                        cellMap.put(key, new HeatmapCell(xAxis, yAxis, likeCount, avgReactions));
                    }
                }
                break;
                
            case "MONTHLY": // y=WeekOfMonth, x=DayName
                yLabels = Arrays.asList(WEEKS_OF_MONTH);
                xLabels = Arrays.asList(DAYS_OF_WEEK);
                
                // Pre-populate the map with 0-value cells
                for (String y : yLabels) {
                    for (String x : xLabels) {
                        cellMap.put(y + ":" + x, new HeatmapCell(x, y, 0L, 0.0));
                    }
                }
                
                // Update with actual data
                for (Object[] row : results) {
                    String yAxis = (String) row[0];
                    String xAxis = (String) row[1];
                    Long likeCount = ((Number) row[2]).longValue();
                    Double avgReactions = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
                    String key = yAxis + ":" + xAxis;
                    if (cellMap.containsKey(key)) {
                        cellMap.put(key, new HeatmapCell(xAxis, yAxis, likeCount, avgReactions));
                    }
                }
                break;
                
            case "YEARLY":
                // GitHub-style: Generate all days in the year
                return generateGitHubStyleHeatmap(results, startDate, endDate);
                
            default:
                throw new IllegalArgumentException("Invalid timeframe: " + timeFrame);
        }

        return new HeatmapData(
                xLabels,
                yLabels,
                new ArrayList<>(cellMap.values()),
                timeFrame
        );
    }

    /**
     * Generate GitHub-style heatmap data.
     * X-axis: Weeks (with month labels showing at the first week of each month)
     * Y-axis: Day of week (Mon, Tue, Wed, Thu, Fri, Sat, Sun) - GitHub order
     * Each cell represents one day
     */
    private HeatmapData generateGitHubStyleHeatmap(List<Object[]> results, LocalDateTime startDate, LocalDateTime endDate) {
        // Create a map of date -> data from SQL results
        Map<String, Object[]> dateDataMap = new HashMap<>();
        for (Object[] row : results) {
            String date = (String) row[1]; // YYYY-MM-DD format
            dateDataMap.put(date, row);
        }
        
        // Start from first Sunday of the year (GitHub week starts on Sunday)
        LocalDate start = LocalDate.of(startDate.getYear(), 1, 1);
        LocalDate end = LocalDate.of(endDate.getYear(), 12, 31);
        
        // Find the first Sunday on or before January 1st
        LocalDate firstSunday = start;
        while (firstSunday.getDayOfWeek().getValue() != 7) { // 7 = Sunday
            firstSunday = firstSunday.minusDays(1);
        }
        
        // Generate all weeks in the year
        List<LocalDate> weekStarts = new ArrayList<>();
        LocalDate current = firstSunday;
        while (current.isBefore(end.plusWeeks(1))) {
            weekStarts.add(current);
            current = current.plusWeeks(1);
        }
        
        // Generate x-axis labels (month names at first occurrence in grid)
        List<String> xLabels = new ArrayList<>();
        Set<Integer> shownMonths = new HashSet<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        
        for (LocalDate weekStart : weekStarts) {
            String monthLabel = "";
            
            // Check if any day in this week is in a month we haven't shown yet
            for (int i = 0; i < 7; i++) {
                LocalDate day = weekStart.plusDays(i);
                int monthValue = day.getMonthValue();
                
                // Show month label if this is the first time we see this month in the grid
                if (!shownMonths.contains(monthValue) && 
                    !day.isBefore(start.withMonth(1).withDayOfMonth(1)) && 
                    !day.isAfter(end.withMonth(12).withDayOfMonth(31))) {
                    monthLabel = day.format(monthFormatter);
                    shownMonths.add(monthValue);
                    break;
                }
            }
            
            xLabels.add(monthLabel);
        }
        
        // Y-axis labels (days of week) - GitHub displays Mon-Sun (Monday at top)
        // But data is stored with Sunday as day 0, so we need to map correctly
        List<String> yLabels = Arrays.asList(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
        );
        
        // Generate cells: one cell per day
        List<HeatmapCell> cells = new ArrayList<>();
        
        for (int weekIndex = 0; weekIndex < weekStarts.size(); weekIndex++) {
            LocalDate weekStart = weekStarts.get(weekIndex);
            String weekLabel = "W" + weekIndex; // Unique identifier for each week column
            
            // Generate cells in GitHub order: Mon, Tue, Wed, Thu, Fri, Sat, Sun
            // Week starts on Sunday (day 0), so we need to reorder
            int[] dayOrder = {1, 2, 3, 4, 5, 6, 0}; // Mon=1, Tue=2, ..., Sat=6, Sun=0
            
            for (int displayRow = 0; displayRow < 7; displayRow++) {
                int dayOfWeek = dayOrder[displayRow];
                LocalDate date = weekStart.plusDays(dayOfWeek);
                String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String yLabel = yLabels.get(displayRow); // Mon, Tue, Wed, Thu, Fri, Sat, Sun
                
                if (dateDataMap.containsKey(dateStr)) {
                    Object[] data = dateDataMap.get(dateStr);
                    Long likeCount = ((Number) data[2]).longValue();
                    Double avgReactions = data[3] != null ? ((Number) data[3]).doubleValue() : 0.0;
                    cells.add(new HeatmapCell(weekLabel, yLabel, likeCount, avgReactions));
                } else {
                    // No data for this day
                    cells.add(new HeatmapCell(weekLabel, yLabel, 0L, 0.0));
                }
            }
        }
        
        return new HeatmapData(xLabels, yLabels, cells, "YEARLY");
    }
}