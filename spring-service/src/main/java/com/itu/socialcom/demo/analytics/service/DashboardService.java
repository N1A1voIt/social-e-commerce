package com.itu.socialcom.demo.analytics.service;

import com.itu.socialcom.demo.analytics.dto.DashboardStatsDto;
import com.itu.socialcom.demo.analytics.dto.PagesRepartitionDto;
import com.itu.socialcom.demo.analytics.dto.PlatformRepartitionDto;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.repository.OrderMotherCplRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Service
public class DashboardService {
    
    @Autowired
    private OrderParentRepository orderParentRepository;
    @Autowired
    private OrderMotherCplRepository orderMotherCplRepository;
    @Autowired
    private EntityManager entityManager;


    public DashboardStatsDto getDashboardStats(Integer sellerId) {
        // Get orders with status >= 25 for the seller
        List<OrderParent> completedOrders = orderParentRepository.findByDStatusGreaterThanEqualAndIdSeller(25, sellerId);
        
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
        DashboardStatsDto dashboardStatsDto = new DashboardStatsDto(totalRevenue, revenuePerUser, bestDeal, totalSales, dateRange);
        PlatformRepartitionDto[] platformRepartitionDtos = platformRepartitionDtos(sellerId);
        PagesRepartitionDto[] pagesRepartitionDtos = pagesRepartitionDtos(sellerId);
        dashboardStatsDto.setPlatformRepartition(platformRepartitionDtos);
        dashboardStatsDto.setPagesRepartition(pagesRepartitionDtos);
        return dashboardStatsDto;
    }
    public PlatformRepartitionDto[] platformRepartitionDtos(Integer sellerId) {
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
                                "WHERE v_order_mother_cpl.id_seller = :sellerId " +
                                "GROUP BY id_sp,total_revenue.total_revenue"
                ).setParameter("sellerId", sellerId)
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

    public PagesRepartitionDto[] pagesRepartitionDtos(Integer sellerId) {
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
                                "WHERE v_order_mother_cpl.id_seller = :sellerId " +
                                "GROUP BY mp.id_mp, mp.id_sp, mp.page_title, mp.associated_media, total_revenue.total_revenue"
                )
                .setParameter("sellerId", sellerId)
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


}
