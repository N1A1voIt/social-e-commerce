package com.itu.socialcom.demo.stocks.services;

import com.itu.socialcom.demo.stocks.StockParent;
import com.itu.socialcom.demo.stocks.repository.StockParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockServiceImpl implements StockService {
    @Autowired
    StockUpdatingService stockUpdatingService;
    @Autowired
    StockParentRepository stockParentRepository;
    @Override
    public StockParent save(StockParent stockParent) {
        return stockUpdatingService.saveStock(stockParent);
    }

    @Override
    public List<StockParent> findAll() {
        return stockParentRepository.findAll();
    }

}
