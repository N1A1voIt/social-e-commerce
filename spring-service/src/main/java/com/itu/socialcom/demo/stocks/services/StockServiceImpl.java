package com.itu.socialcom.demo.stocks.services;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import com.itu.socialcom.demo.stocks.StockParent;
import com.itu.socialcom.demo.stocks.dto.StockUtilities;
import com.itu.socialcom.demo.stocks.repository.StockParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {
    @Autowired
    StockUpdatingService stockUpdatingService;
    @Autowired
    StockParentRepository stockParentRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    VariantRepository variantRepository;
    @Autowired
    private StockPersistanceService stockPersistanceService;

    @Override
    public StockParent save(StockParent stockParent) {
        stockParent.setCreatedAt(LocalDateTime.now());
        return stockPersistanceService.saveStock(stockParent);
    }

    @Override
    public List<StockParent> findAll() {
        return stockParentRepository.findAll();
    }

    public StockUtilities getStockUtilities(Seller seller, Pageable pageable) {
        StockUtilities stockUtilities = new StockUtilities();
        stockUtilities.setProducts(productRepository.findByIdSeller(seller.getId().intValue(),pageable).getContent());
        List<Long> products = new ArrayList<>();
        for (Product product : stockUtilities.getProducts()) {
            products.add(product.getIdProduct());
        }
        stockUtilities.setVariants(variantRepository.findByIdProductIn(products));
        return stockUtilities;
    }

}
