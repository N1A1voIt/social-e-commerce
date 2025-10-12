package com.itu.socialcom.demo.analytics.service;

import com.itu.socialcom.demo.analytics.dto.DashboardStatsDto;
import com.itu.socialcom.demo.analytics.dto.PlatformRepartitionDto;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.repository.OrderMotherCplRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
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
        
        return new DashboardStatsDto(totalRevenue, revenuePerUser, bestDeal, totalSales, dateRange);
    }
    public PlatformRepartitionDto[] platformRepartitionDtos(Integer sellerId) {
        HashMap<Integer, String> platformMap = new HashMap<>();
        platformMap.put(1, "Facebook");
        platformMap.put(2, "Instagram");
        List<OrderParent> completedOrders = orderParentRepository.findByDStatusGreaterThanEqualAndIdSeller(25, sellerId);
        long facebookCount = completedOrders.stream()
                .filter(order -> order.getIdPc() != null && order.getIdPc().startsWith("FB"))
                .count();
        long instagramCount = completedOrders.stream()
                .filter(order -> order.getIdPc() != null && order.getIdPc().startsWith("IG"))
                .count();
        long whatsappCount = completedOrders.stream()
                .filter(order -> order.getIdPc() != null && order.getIdPc().startsWith("WA"))
                .count();
        long otherCount = completedOrders.size() - (facebookCount + instagramCount + whatsappCount);
        PlatformRepartitionDto facebookDto = new PlatformRepartitionDto("Facebook", facebookCount);
        PlatformRepartitionDto instagramDto = new PlatformRepartitionDto("Instagram", instagramCount);
        PlatformRepartitionDto whatsappDto = new PlatformRepartitionDto("WhatsApp", whatsappCount);
        PlatformRepartitionDto otherDto = new PlatformRepartitionDto("Other", otherCount);
        return new PlatformRepartitionDto[]{facebookDto, instagramDto, whatsappDto, otherDto};

    }
}
