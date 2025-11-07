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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public void cancelOrder(RefundRequest refundRequest) {
        OrderParent orderParent = orderParentRepository.findById(refundRequest.getOrderId().longValue()).orElseThrow(() -> new RuntimeException("Order not found"));
        boolean isRefunded = orderParent.getDStatus() >= 11;
        if (isRefunded) {
            Sales sales = fetchSale(orderParent);
            Refund refund = createRefund(sales,orderParent);
            refundRepository.save(refund);
            sales.setStatus(21);
            salesRepository.save(sales);
            StockParent stockParent = fetchStockParent(orderParent);
            stockParentRepository.save(stockParent);
            stockChildRepository.saveAll(stockParent.getItems());
        }
        orderParent.setDStatus(21);
        orderParentRepository.save(orderParent);
    }

    private StockParent fetchStockParent(OrderParent orderParent) {
        StockParent stockParent = stockParentRepository.findByIdOrderM(orderParent.getIdOrderM());
        List<StockChild> stockChildren = stockChildRepository.findByIdMv(stockParent.getId());
        StockParent stockParent1 = new StockParent();
        stockParent1.setCreatedAt(stockParent.getCreatedAt());
        stockParent1.setDescription(stockParent.getDescription());
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
    private Sales fetchSale(OrderParent orderParent) {
        return salesRepository.findByIdOrderM(orderParent.getIdOrderM().intValue()).orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    private Refund createRefund(Sales sales,OrderParent orderParent) {
        Refund refund = new Refund();
        refund.setOrderId(orderParent.getIdOrderM());
        refund.setSaleId(sales.getIdSale());
        refund.setAmount(sales.getPaidAmount());
        refund.setCreatedAt(LocalDateTime.now());
        return refund;
    }

}
