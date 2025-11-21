package com.itu.socialcom.demo.analytics.service;

import com.itu.socialcom.demo.analytics.dto.HeatmapCell;
import com.itu.socialcom.demo.analytics.dto.HeatmapData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class HeatmapService {

    @Autowired
    private EntityManager entityManager;

    // Fixed Y-Axis labels for GitHub style (Monday to Sunday)
    private static final List<String> DAYS_OF_WEEK = Arrays.asList(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    );

    public HeatmapData getHeatmapData(LocalDateTime startDate, LocalDateTime endDate, Integer sellerId) {
        // 1. Execute the simplified Daily Snapshot Query
        String sql = buildDailySnapshotQuery();

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("sellerId", sellerId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // 2. Transform into Grid Data
        return transformToYearlyGrid(results, startDate, endDate);
    }

    private String buildDailySnapshotQuery() {
        return """
            WITH daily_end AS (
                 SELECT
                     id_child,
                     CAST(created_at AS DATE) AS date_key,
                     reactions,
                     ROW_NUMBER() OVER (
                         PARTITION BY id_child, CAST(created_at AS DATE)
                         ORDER BY created_at DESC
                     ) AS rn
                 FROM v_likes_history_post_child
                 WHERE reactions IS NOT NULL
                   AND created_at >= :startDate
                   AND created_at <= :endDate
                   AND id_seller = :sellerId
             ),
             daily_final AS (
                 SELECT id_child, date_key, reactions
                 FROM daily_end
                 WHERE rn = 1
             ),
             with_prev AS (
                 SELECT
                     d.*,
                     LAG(reactions) OVER (
                         PARTITION BY id_child ORDER BY date_key
                     ) AS prev_reactions
                 FROM daily_final d
             )
             SELECT
                  TO_CHAR(date_key, 'YYYY-MM-DD') AS day_str,
                 SUM(COALESCE(reactions - prev_reactions, 0)) AS final_likes
             FROM with_prev
             GROUP BY date_key
             ORDER BY date_key;

        """;
    }

    private HeatmapData transformToYearlyGrid(List<Object[]> results, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // 1. Map SQL results to a lookup Map (DateString -> Count)
        Map<String, Long> dataMap = new HashMap<>();
        for (Object[] row : results) {
            String dateStr = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            dataMap.put(dateStr, count);
        }

        LocalDate start = startDateTime.toLocalDate();
        LocalDate end = endDateTime.toLocalDate();
        List<HeatmapCell> cells = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        // 2. Align Start Date to the previous Monday (to make the grid square)
        // In GitHub style, the grid usually starts on the first day of the week relative to the start date
        LocalDate current = start;
        while (current.getDayOfWeek() != DayOfWeek.MONDAY) {
            current = current.minusDays(1);
        }

        // 3. Iterate week by week until we pass the end date
        int weekIndex = 0;
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM");
        DateTimeFormatter keyFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (current.isBefore(end) || current.isEqual(end)) {

            // Logic for X-Axis Label (Show Month name if it's the first week of that month)
            LocalDate midWeek = current.plusDays(3); // Use middle of week to determine month label
            String xLabel = "";
            if (weekIndex == 0 || midWeek.getDayOfMonth() <= 7) {
                xLabel = midWeek.format(monthFmt);
            }
            xLabels.add(xLabel);

            // Logic for Cells (7 days per week column)
            for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                LocalDate dayDate = current.plusDays(dayOffset);
                String dayKey = dayDate.format(keyFmt);

                // Y-Axis Label (Mon, Tue, etc.)
                String yLabel = DAYS_OF_WEEK.get(dayOffset);

                // Check if this specific day is actually within the requested range
                // (Since we padded the start to Monday, some days might be before startDate)
                boolean inRange = !dayDate.isBefore(start) && !dayDate.isAfter(end);

                Long value = 0L;
                if (inRange && dataMap.containsKey(dayKey)) {
                    value = dataMap.get(dayKey);
                }

                // Create Cell: X=WeekIndex (or specific date if you prefer), Y=DayOfWeek
                // Note: 'x' here represents the column index
                cells.add(new HeatmapCell(String.valueOf(weekIndex), yLabel, value, 0.0));
            }

            // Move to next week
            current = current.plusWeeks(1);
            weekIndex++;
        }

        return new HeatmapData(
                xLabels,         // Month names aligned to weeks
                DAYS_OF_WEEK,    // Mon-Sun
                cells,
                "YEARLY"
        );
    }
}