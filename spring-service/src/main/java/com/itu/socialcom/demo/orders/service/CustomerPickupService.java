package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.orders.OrderChild;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.repository.OrderChildRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.sales.Payments;
import com.itu.socialcom.demo.sales.PaymentsRepository;
import com.itu.socialcom.demo.sales.Sales;
import com.itu.socialcom.demo.sales.SalesDetails;
import com.itu.socialcom.demo.sales.SalesRepository;
import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import com.itu.socialcom.demo.stocks.services.StockPersistanceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling customer pickup orders
 * Status flow: 1 (Created) -> 26 (Waiting for customer) -> 51 (Completed)
 */
@Service
public class CustomerPickupService {

    @Autowired
    private OrderParentRepository orderParentRepository;

    @Autowired
    private OrderChildRepository orderChildRepository;

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private StockPersistanceService stockPersistanceService;

    /**
     * Set order to customer pickup mode
     * Changes status from 1 (Created) to 26 (Waiting for customer)
     * Creates a sale with paid amount = 0
     * Moves stock
     */
    @Transactional
    public OrderParent setCustomerPickup(Long orderId) throws Exception {
        try {
            // Find the order
            OrderParent orderParent = orderParentRepository.findById(orderId)
                    .orElseThrow(() -> new Exception("Order not found with ID: " + orderId));

            // Validate current status
            if (orderParent.getDStatus() != 1) {
                throw new Exception("Order must be in Created status (1) to set as customer pickup. Current status: " + orderParent.getDStatus());
            }

            // Change status to 26 (Waiting for customer)
            orderParent.setDStatus(26);
            orderParentRepository.save(orderParent);

            // Get order children
            List<OrderChild> orderChildren = orderChildRepository.findByIdOrderM(orderId);

            // Create sale with paid amount = 0
            createSaleForPickup(orderParent, orderChildren);

            // Move stock
            moveStock(orderParent);

            return orderParent;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to set customer pickup: " + e.getMessage());
        }
    }

    /**
     * Complete customer pickup
     * Changes status from 26 (Waiting for customer) to 51 (Completed)
     * Creates cash payment for the full amount
     */
    @Transactional
    public OrderParent completeCustomerPickup(Long orderId) throws Exception {
        try {
            // Find the order
            OrderParent orderParent = orderParentRepository.findById(orderId)
                    .orElseThrow(() -> new Exception("Order not found with ID: " + orderId));

            // Validate current status
            if (orderParent.getDStatus() != 26) {
                throw new Exception("Order must be in Waiting for customer status (26) to complete pickup. Current status: " + orderParent.getDStatus());
            }

            // Find the associated sale
            Sales sales = salesRepository.findByIdOrderM(orderId.intValue())
                    .orElseThrow(() -> new Exception("Sale not found for order ID: " + orderId));

            // Create cash payment for the full amount
            double totalAmount = sales.getAmount().doubleValue();
            Payments payment = new Payments();
            payment.setAmount(totalAmount);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setIdSales(sales.getIdSale().longValue());
            payment.setIdPm(2L); // Cash payment method ID
            payment.setPaymentMethod("Cash");
            paymentsRepository.save(payment);

            // Update sale paid amount to full
            sales.setPaidAmount(totalAmount);
            sales.setStatus(11); // Fully paid
            salesRepository.save(sales);

            // Change order status to 51 (Completed)
            orderParent.setDStatus(51);
            orderParentRepository.save(orderParent);

            return orderParent;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to complete customer pickup: " + e.getMessage());
        }
    }

    /**
     * Create a sale for pickup order with paid amount = 0
     */
    private Sales createSaleForPickup(OrderParent orderParent, List<OrderChild> orderChildren) throws Exception {
        if (orderParent == null) {
            throw new Exception("OrderParent cannot be null");
        }

        // Create the Sales entity
        Sales sales = new Sales();

        // Map basic fields
        sales.setAmount(orderParent.getDTotal() != null
                ? BigDecimal.valueOf(orderParent.getDTotal())
                : BigDecimal.ZERO);
        sales.setEffectuatedAt(orderParent.getCreatedAt() != null
                ? orderParent.getCreatedAt()
                : LocalDateTime.now());
        sales.setFromNumber(orderParent.getCustomerNumber() != null
                ? orderParent.getCustomerNumber()
                : "");
        sales.setFromName(orderParent.getDCustomerName() != null
                ? orderParent.getDCustomerName()
                : "");
        sales.setDescription(orderParent.getDescription());
        sales.setIdOrderM(orderParent.getIdOrderM() != null
                ? orderParent.getIdOrderM().intValue()
                : null);
        sales.setIdPc(orderParent.getIdPc() != null
                ? orderParent.getIdPc()
                : "");
        sales.setIdSeller(orderParent.getIdSeller());

        // Set paid amount to 0 for pickup orders
        sales.setPaidAmount(0.0);
        sales.setStatus(1); // Unpaid / Pending payment

        // Create sales details from order children
        List<SalesDetails> salesDetailsList = new ArrayList<>();
        if (orderChildren != null && !orderChildren.isEmpty()) {
            for (OrderChild orderChild : orderChildren) {
                SalesDetails salesDetail = new SalesDetails();
                salesDetail.setPrice(orderChild.getPrice() != null
                        ? BigDecimal.valueOf(orderChild.getPrice())
                        : BigDecimal.ZERO);
                salesDetail.setQuantity(orderChild.getQuantity() != null
                        ? BigDecimal.valueOf(orderChild.getQuantity())
                        : BigDecimal.ONE);
                salesDetail.setProductName(orderChild.getProductName());
                salesDetail.setVariantName(orderChild.getSku());
                salesDetail.setIdProduct(orderChild.getIdProduct() != null
                        ? orderChild.getIdProduct().intValue()
                        : 0);
                salesDetail.setIdVariant(orderChild.getIdVariant() != null
                        ? orderChild.getIdVariant().intValue()
                        : 0);
                salesDetail.setSale(sales);
                salesDetailsList.add(salesDetail);
            }
        }

        sales.setDetails(salesDetailsList);
        return salesRepository.save(sales);
    }

    /**
     * Move stock for the order
     */
    private StockParent moveStock(OrderParent parent) {
        List<OrderChild> orderChildren = orderChildRepository.findByIdOrderM(parent.getIdOrderM());
        List<StockChild> stockChildren = new ArrayList<>();

        for (OrderChild orderChild : orderChildren) {
            StockChild stockChild = new StockChild();
            stockChild.setPrice(orderChild.getPrice());
            stockChild.setProductName(orderChild.getProductName());
            stockChild.setVariantName(orderChild.getProductName());
            stockChild.setIdProduct(orderChild.getIdProduct());
            stockChild.setIdVariant(orderChild.getIdVariant());
            stockChild.setOutput(orderChild.getQuantity());
            stockChild.setInput(0.0);
            stockChild.setActionAt(LocalDateTime.now());
            stockChild.setCreatedAt(LocalDateTime.now());
            stockChildren.add(stockChild);
        }

        StockParent stockParent = new StockParent();
        stockParent.setCreatedAt(LocalDateTime.now());
        stockParent.setDescription("Move of order " + parent.getDescription() + " (Customer Pickup)");
        stockParent.setItems(stockChildren);
        stockParent.setIdSeller(parent.getIdSeller().longValue());
        stockParent.setIdOrderM(parent.getIdOrderM());

        return stockPersistanceService.saveStock(stockParent);
    }
}
