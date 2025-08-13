package com.itu.socialcom.demo.stocks.services;

import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockUpdatingService extends StockSavingService{
    @Transactional
    @Override
    public StockParent saveStock(StockParent stockParent) {
        stockParentRepository.save(stockParent);
        for (StockChild stockChild : stockParent.getStockChildren()) {
            stockChild.setIdMv(stockParent.getId());
            stockChildRepository.save(stockChild);
        }
        return stockParent;
    }
}
