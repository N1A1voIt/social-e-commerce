package com.itu.socialcom.demo.analytics.service;

import com.itu.socialcom.demo.analytics.dto.*;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.repository.OrderMotherCplRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    
    @Autowired
    private OrderParentRepository orderParentRepository;
    @Autowired
    private OrderMotherCplRepository orderMotherCplRepository;
    @Autowired
    private EntityManager entityManager;


    public DashboardStatsDto getDashboardStats(Integer sellerId, DashboardRequestDto dashboardRequestDto) {
        // Get orders with status >= 25 for the seller
        List<OrderParent> completedOrders = orderParentRepository.findByDStatusGreaterThanEqualAndIdSeller(25, sellerId,
                dashboardRequestDto.getStartDate(), dashboardRequestDto.getEndDate());
        
        // Calculate total revenue
        Double totalRevenue = completedOrders.stream()
                .mapToDouble(OrderParent::getDTotal)
                .sum();
        
        // Calculate revenue per user (average revenue per customer)
        Double revenuePerUser = 0.0;
        if (!completedOrders.isEmpty()) {
            long uniqueCustomers = completedOrders.stream()
                    .map(OrderParent::getIdPc)
                    .distinct()
                    .count();
            revenuePerUser = uniqueCustomers > 0 ? totalRevenue / uniqueCustomers : 0.0;
        }
        
        // Find best deal (highest total order)
        Double bestDeal = completedOrders.stream()
                .mapToDouble(OrderParent::getDTotal)
                .max()
                .orElse(0.0);
        
        // Count total sales
        Long totalSales = (long) completedOrders.size();
        
        // Generate date range (last 30 days for now)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        String dateRange = thirtyDaysAgo.format(DateTimeFormatter.ofPattern("MMM d")) + 
                          " to " + now.format(DateTimeFormatter.ofPattern("MMM d yyyy"));
        BestTimeToPost bestTimeToPost = bestPostTimeForBetterAttendance(sellerId,dashboardRequestDto);
        DashboardStatsDto dashboardStatsDto = new DashboardStatsDto(totalRevenue, revenuePerUser, bestDeal, totalSales, dateRange);
        PlatformRepartitionDto[] platformRepartitionDtos = platformRepartitionDtos(sellerId,dashboardRequestDto);
        PagesRepartitionDto[] pagesRepartitionDtos = pagesRepartitionDtos(sellerId,dashboardRequestDto);
        dashboardStatsDto.setPlatformRepartition(platformRepartitionDtos);
        dashboardStatsDto.setPagesRepartition(pagesRepartitionDtos);
        dashboardStatsDto.setBestTimeToPost(bestTimeToPost);
        dashboardStatsDto.setSalesProgressionDto(getSalesProgression(dashboardRequestDto,sellerId));
        return dashboardStatsDto;
    }
    public PlatformRepartitionDto[] platformRepartitionDtos(Integer sellerId, DashboardRequestDto dashboardRequestDto) {
        List<Object[]> results = entityManager.createNativeQuery(
                        "WITH total_revenue AS ( " +
                                "    SELECT SUM(d_total) AS total_revenue " +
                                "    FROM order_mother " +
                                "    WHERE id_seller = :sellerId  " +
                                ") " +
                                "SELECT row_number() over () as dummy_id, " +
                                "       sum(d_total)/total_revenue.total_revenue * 100 as total_percentage, " +
                                "       sum(d_total) as total, id_sp " +
                                "FROM v_order_mother_cpl " +
                                "CROSS JOIN total_revenue " +
                                "WHERE v_order_mother_cpl.id_seller = :sellerId AND v_order_mother_cpl.created_at >= :startDate AND v_order_mother_cpl.created_at <= :endDate " +
                                "GROUP BY id_sp,total_revenue.total_revenue"
                ).setParameter("sellerId", sellerId)
                .setParameter("startDate", dashboardRequestDto.getStartDate())
                .setParameter("endDate", dashboardRequestDto.getEndDate())
                .getResultList();

        List<PlatformRepartitionDto> dtos = results.stream()
                .map(r -> new PlatformRepartitionDto(
                        ((Number) r[0]).intValue(),    // dummy_id
                        ((Number) r[1]).doubleValue(), // total_percentage
                        ((Number) r[2]).doubleValue(), // total
                        ((Number) r[3]).intValue()   // idSp
                ))
                .toList();

        return dtos.toArray(new PlatformRepartitionDto[0]);
    }

    public BestTimeToPost bestPostTimeForBetterAttendance(Integer sellerId, DashboardRequestDto dashboardRequestDto) {
        // Implementation pending
        List<Object[]> results = entityManager.createNativeQuery(
                        "WITH engagement_by_datetime AS ( " +
                                "    SELECT " +
                                "        EXTRACT(DOW FROM created_at) AS day_of_week, " +
                                "        EXTRACT(HOUR FROM created_at) AS hour_of_day, " +
                                "        COUNT(*) AS total_posts, " +
                                "        SUM(reactions) AS total_reactions, " +
                                "        AVG(reactions) AS avg_reactions, " +
                                "        PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY reactions) AS median_reactions " +
                                "    FROM v_likes_history_post_child " +
                                "    WHERE reactions IS NOT NULL AND id_seller = :sellerId AND created_at <= :endDate AND created_at >= :startDate" +
                                "    GROUP BY " +
                                "        EXTRACT(DOW FROM created_at), " +
                                "        EXTRACT(HOUR FROM created_at) " +
                                ") " +
                                "SELECT " +
                                "    CASE day_of_week " +
                                "        WHEN 0 THEN 'Sunday' " +
                                "        WHEN 1 THEN 'Monday' " +
                                "        WHEN 2 THEN 'Tuesday' " +
                                "        WHEN 3 THEN 'Wednesday' " +
                                "        WHEN 4 THEN 'Thursday' " +
                                "        WHEN 5 THEN 'Friday' " +
                                "        WHEN 6 THEN 'Saturday' " +
                                "        END AS best_day, " +
                                "    hour_of_day AS best_hour, " +
                                "    total_posts, " +
                                "    total_reactions, " +
                                "    ROUND(avg_reactions, 2) AS avg_reactions, " +
                                "    median_reactions AS median_reactions " +
                                "FROM engagement_by_datetime " +
                                "WHERE total_posts >= 5 " +
                                "ORDER BY avg_reactions DESC " +
                                "LIMIT 1"
                ).setParameter("sellerId", sellerId)
                .setParameter("startDate", dashboardRequestDto.getStartDate())
                .setParameter("endDate", dashboardRequestDto.getEndDate())
                .getResultList();
        List<BestTimeToPost> dtos = results.stream()
                .map(r -> new BestTimeToPost(
                        ((String) r[0]),
                        ((Number) r[1]).intValue()
                ))
                .toList();
        if (!dtos.isEmpty()) {
            BestTimeToPost bestTimeToPost = dtos.get(0);
            return bestTimeToPost;
        }
        return new BestTimeToPost();
    }

    public PagesRepartitionDto[] pagesRepartitionDtos(Integer sellerId, DashboardRequestDto dashboardRequestDto) {
        List<Object[]> results = entityManager.createNativeQuery(
                        "WITH total_revenue AS ( " +
                                "    SELECT SUM(d_total) AS total_revenue " +
                                "    FROM order_mother " +
                                "    WHERE id_seller = :sellerId " +
                                ") " +
                                "SELECT  " +
                                "   row_number() over () as dummy_id, " +
                                "   COALESCE(sum(d_total)/total_revenue.total_revenue * 100,0) as total_percentage, " +
                                "   mp.page_title, " +
                                "   mp.associated_media, " + // 4th column
                                "   COALESCE(sum(d_total),0) as total, " +
                                "   mp.id_mp as id_managed_pages, " +
                                "   mp.id_sp " +
                                "FROM v_order_mother_cpl " +
                                "RIGHT JOIN managed_pages mp on v_order_mother_cpl.id_managed_pages = mp.id_mp " +
                                "CROSS JOIN total_revenue " +
                                "WHERE v_order_mother_cpl.id_seller = :sellerId AND v_order_mother_cpl.created_at >= :startDate AND v_order_mother_cpl.created_at <= :endDate " +
                                "GROUP BY mp.id_mp, mp.id_sp, mp.page_title, mp.associated_media, total_revenue.total_revenue"
                )
                .setParameter("sellerId", sellerId)
                .setParameter("startDate", dashboardRequestDto.getStartDate())
                .setParameter("endDate", dashboardRequestDto.getEndDate())
                .getResultList();

        List<PagesRepartitionDto> dtos = results.stream()
                .map(r -> new PagesRepartitionDto(
                        ((Number) r[0]).intValue(),   // dummy_id
                        ((Number) r[1]).doubleValue(),// total_percentage
                        ((Number) r[4]).doubleValue(),// total
                        (String) r[2],                // page_title
                        ((Number) r[6]).intValue(),   // id_sp
                        ((Number) r[5]).intValue(),   // id_managed_pages
                        (String) r[3]                 // associated_media
                ))
                .toList();

        return dtos.toArray(new PagesRepartitionDto[0]);
    }

    public SalesProgressionDto getSalesProgression(DashboardRequestDto dashboardRequestDto, Integer sellerId) {

        // Determine if we should apply the date filter
        boolean applyDateFilter = !(dashboardRequestDto.getStartDate().toLocalDate().equals(LocalDate.of(1990,1,1))
                && dashboardRequestDto.getEndDate().toLocalDate().equals(LocalDate.of(3000,1,1)));

        StringBuilder sql = new StringBuilder(
                "SELECT DATE(created_at) AS sale_date, SUM(d_total) AS total_sales " +
                        "FROM order_mother " +
                        "WHERE id_seller = :sellerId "
        );

        if (applyDateFilter) {
            sql.append("AND (created_at >= :startDate) AND (created_at <= :endDate) ");
        }

        sql.append("GROUP BY sale_date ORDER BY sale_date");

        var query = entityManager.createNativeQuery(sql.toString())
                .setParameter("sellerId", sellerId);

        if (applyDateFilter) {
            query.setParameter("startDate", dashboardRequestDto.getStartDate())
                    .setParameter("endDate", dashboardRequestDto.getEndDate());
        }

        List<Object[]> results = query.getResultList();

        // Step 1: map query results and generate labels & data only for dates that have sales
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        for (Object[] row : results) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            LocalDate date = sqlDate.toLocalDate();
            Double total = ((Number) row[1]).doubleValue();

            labels.add(date.toString()); // or format to "10 Oct" if you want
            data.add(total);
        }

        return new SalesProgressionDto(labels, data);
    }
}
