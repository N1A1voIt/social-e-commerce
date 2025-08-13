package com.itu.socialcom.demo.stocks.services;

import com.itu.socialcom.demo.stocks.StockParent;
import com.itu.socialcom.demo.stocks.repository.StockChildRepository;
import com.itu.socialcom.demo.stocks.repository.StockParentRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class StockSavingService {
    @Autowired
    StockParentRepository stockParentRepository;
    @Autowired
    StockChildRepository stockChildRepository;

    abstract StockParent saveStock(StockParent stockParent);
}
