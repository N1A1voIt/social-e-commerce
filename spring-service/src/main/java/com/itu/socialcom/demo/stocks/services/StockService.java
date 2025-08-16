package com.itu.socialcom.demo.stocks.services;

import com.itu.socialcom.demo.stocks.StockParent;

import java.util.List;

public interface StockService {
    StockParent save(StockParent stockParent);
    List<StockParent> findAll();
}
