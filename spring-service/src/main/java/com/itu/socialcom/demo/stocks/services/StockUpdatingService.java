package com.itu.socialcom.demo.stocks.services;

import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockUpdatingService extends StockSavingService{

    @Override
    public StockParent saveStock(StockParent stockParent) {
        List<StockChild> stockChildren = stockParent.getStockChildren();

        return null;
    }
}
