package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.Refund;
import com.itu.socialcom.demo.orders.dto.RefundRequest;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.orders.repository.RefundRepository;
import com.itu.socialcom.demo.sales.Sales;
import com.itu.socialcom.demo.sales.SalesRepository;
import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import com.itu.socialcom.demo.stocks.repository.StockChildRepository;
import com.itu.socialcom.demo.stocks.repository.StockParentRepository;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderRefundService {
    @Autowired
    private OrderParentRepository orderParentRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private SalesRepository salesRepository;
    @Autowired
    private StockParentRepository stockParentRepository;
    @Autowired
    private StockChildRepository stockChildRepository;
    @Transactional
    public Refund cancelOrder(RefundRequest refundRequest) {
        OrderParent orderParent = orderParentRepository.findById(refundRequest.getOrderId().longValue()).orElseThrow(() -> new RuntimeException("Order not found"));
        boolean isRefunded = orderParent.getDStatus() >= 11;
        Refund refund = new Refund();
        refund.setAmount(0.0);
        if (isRefunded) {
            Sales sales = fetchSale(orderParent);
            refund = createRefund(sales,orderParent);
            refundRepository.save(refund);
            if (sales != null) {
                sales.setStatus(21);
                salesRepository.save(sales);
            }
            StockParent stockParent = fetchStockParent(orderParent);
            if (stockParent != null) {
                stockParentRepository.save(stockParent);
                for (StockChild child:stockParent.getItems()) {
                    child.setIdMv(stockParent.getId());
                    stockChildRepository.save(child);
                }
            }
        }
        orderParent.setDStatus(21);
        orderParentRepository.save(orderParent);
        return refund;
    }

    @Transactional
    StockParent fetchStockParent(OrderParent orderParent) {
        StockParent stockParent = stockParentRepository.findByIdOrderM(orderParent.getIdOrderM());
        if (stockParent == null) return null;
        List<StockChild> stockChildren = stockChildRepository.findByIdMv(stockParent.getId());
        StockParent stockParent1 = new StockParent();
        stockParent1.setCreatedAt(stockParent.getCreatedAt());
        stockParent1.setDescription("Refund impact : " + stockParent.getDescription());
        stockParent1.setIdSeller(stockParent.getIdSeller());
        stockParent1.setIdOrderM(stockParent.getIdOrderM());

        List<StockChild> stockChildren1 = new ArrayList<>();

        for (StockChild stockChild : stockChildren) {
            StockChild stockChild1 = new StockChild();
            stockChild1.setPrice(stockChild.getPrice());
            stockChild1.setProductName(stockChild.getProductName());
            stockChild1.setVariantName(stockChild.getVariantName());
            stockChild1.setIdProduct(stockChild.getIdProduct());
            stockChild1.setIdVariant(stockChild.getIdVariant());
            stockChild1.setOutput(0.0);
            stockChild1.setInput(stockChild.getOutput());
            stockChild1.setActionAt(stockChild.getActionAt());
            stockChild1.setCreatedAt(stockChild.getCreatedAt());
            stockChildren1.add(stockChild1);
        }

        stockParent1.setItems(stockChildren1);
        return stockParent1;
    }
    @Transactional
    Sales fetchSale(OrderParent orderParent) {
        Optional<Sales> salesOptional = salesRepository.findByIdOrderM(orderParent.getIdOrderM().intValue());
        return salesOptional.orElse(null);
    }

    @Transactional
    Refund createRefund(Sales sales,OrderParent orderParent) {
        Refund refund = new Refund();
        refund.setOrderId(orderParent.getIdOrderM());
        refund.setSaleId(sales != null ? sales.getIdSale() : null);
        refund.setAmount(sales != null ? sales.getPaidAmount() : 0.0);
        refund.setCreatedAt(LocalDateTime.now());
        return refund;
    }

}
