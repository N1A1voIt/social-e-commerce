package com.itu.socialcom.demo.stocks.services;

import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class StockUpdatingService extends StockSavingService{
    @Transactional
    @Override
    public StockParent saveStock(StockParent stockParent) {
        stockParent.setCreatedAt(LocalDateTime.now());
        stockParentRepository.save(stockParent);
        for (StockChild stockChild : stockParent.getItems()) {
            stockChild.setIdMv(stockParent.getId());
            stockChild.setCreatedAt(LocalDateTime.now());
            stockChildRepository.save(stockChild);
        }
        return stockParent;
    }
}
