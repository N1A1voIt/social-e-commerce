package com.itu.socialcom.demo.stocks.services;

import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.repository.StockChildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RefillService {

    @Autowired
    private StockChildRepository stockChildRepository;
    @Autowired
    private ProductRepository productRepository;

    public List<String> generateRefillMessages(LocalDateTime fromDate) {

        List<StockChild> sales = stockChildRepository.findByActionAtAfterOrderByIdProductAscActionAtAsc(fromDate);
//        findByActionAtAfterOrderByIdProductAscActionAtAsc
        // Group by product
        Map<Long, List<StockChild>> productSalesMap = sales.stream()
                .collect(Collectors.groupingBy(StockChild::getIdProduct));

        List<String> messages = new ArrayList<>();

        for (Map.Entry<Long, List<StockChild>> entry : productSalesMap.entrySet()) {
            List<StockChild> productSales = entry.getValue();

            double totalSold = productSales.stream().mapToDouble(s -> s.getOutput() != null ? s.getOutput() : 0).sum();
            double currentStock = productSales.get(productSales.size() - 1).getDProductNumber();

            LocalDateTime firstSale = productSales.get(0).getActionAt();
            LocalDateTime lastSale = productSales.get(productSales.size() - 1).getActionAt();

            long days = ChronoUnit.DAYS.between(firstSale.toLocalDate(), lastSale.toLocalDate());
            double avgDailySales = totalSold / Math.max(days, 1);

            double recommendedRefill = Math.ceil(avgDailySales * 7); // 7-day lead time
            double daysUntilEmpty = currentStock / Math.max(avgDailySales, 0.01);

            String message = String.format(
                    "You sold %.0f %s in the last %d days. Current stock is %.0f. Recommended refill: %.0f",
                    totalSold,
                    productSales.get(0).getIdProduct(),
                    days,
                    currentStock,
                    recommendedRefill
            );

            messages.add(message);
        }

        return messages;
    }

    public String generateRefillMessageForProduct(Long productId, LocalDateTime fromDate) {
        Product product = productRepository.findById(productId).orElse(null);
        // Fetch all stock records for the specific product since the given date
        List<StockChild> productSales = stockChildRepository.findByIdProductAndActionAtAfterOrderByActionAtAsc(productId, fromDate);
//        findByIdProductAndActionAtAfterOrderByActionAtAsc
        if (productSales.isEmpty()) {
            return "No sales data available for product ID: " + productId + " since " + fromDate;
        }

        // Compute total sold
        double totalSold = productSales.stream()
                .mapToDouble(s -> s.getOutput() != null ? s.getOutput() : 0)
                .sum();

        // Get the current stock (from the latest record)
        double currentStock = productSales.get(productSales.size() - 1).getDProductNumber();

        // Get first and last sale dates
        LocalDateTime firstSale = productSales.get(0).getActionAt();
        LocalDateTime lastSale = productSales.get(productSales.size() - 1).getActionAt();

        // Compute days between first and last sale
        long days = ChronoUnit.DAYS.between(firstSale.toLocalDate(), lastSale.toLocalDate());
        double avgDailySales = totalSold / Math.max(days, 1);

        // Refill recommendation
        double recommendedRefill = Math.ceil(avgDailySales * 7); // 7-day lead time
        double daysUntilEmpty = currentStock / Math.max(avgDailySales, 0.01);

        // Build message
        return String.format(
                "Product %s: Sold %.0f units in the last %d days. Current stock: %.0f. " +
                        "Recommended refill: %.0f (about %.1f days until stock runs out).",
                product != null ? product.getName() : "Nan",
                totalSold,
                days,
                currentStock,
                recommendedRefill,
                daysUntilEmpty
        );
    }

}
