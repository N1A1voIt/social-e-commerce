package com.itu.socialcom.demo.stocks.services;

import com.itu.socialcom.demo.stocks.InsufficientStockException;
import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class StockPersistanceService extends StockSavingService {
    @Override
    @Transactional
    public StockParent saveStock(StockParent stockParent) throws InsufficientStockException {
        super.stockParentRepository.save(stockParent);
        List<Long> variantsIds = new ArrayList<>();
        Set<Long> productRecords = new HashSet<>();
        for (StockChild stockChild : stockParent.getItems()) {
            variantsIds.add(stockChild.getIdVariant());
            productRecords.add(stockChild.getIdProduct());
        }
        List<StockChild> stockChildren = super.stockChildRepository.findMostRecentVariantsByVariantIds(variantsIds);
        List<StockChild> productChildRecords = super.stockChildRepository.findByLastProductRecords(productRecords.stream().toList());

        HashMap<Long, StockChild> stockChildMap = new HashMap<>();
        HashMap<Long, StockChild> productChildMap = new HashMap<>();
        for (StockChild sc : stockChildren) {
            stockChildMap.put(sc.getIdVariant(), sc);
        }
        for (StockChild sc : productChildRecords) {
            productChildMap.put(sc.getIdProduct(), sc);
        }
        for (StockChild currentChild : stockParent.getItems()) {
            StockChild correspondingVariantChild = stockChildMap.get(currentChild.getIdVariant());
            StockChild productStockRecord = productChildMap.get(currentChild.getIdProduct());
            if (correspondingVariantChild != null && productStockRecord != null) {
                System.out.println("Going right here");
                double newProductNumber = (currentChild.getInput() > 0) ?
                        (productStockRecord.getDProductNumber() + currentChild.getInput()) :
                        (productStockRecord.getDProductNumber() - currentChild.getOutput());
                productStockRecord.setDProductNumber(newProductNumber);
                double newVariantNumber = (currentChild.getInput() > 0) ?
                        (correspondingVariantChild.getDVariantNumber() + currentChild.getInput()) :
                        (correspondingVariantChild.getDVariantNumber() - currentChild.getOutput());
                correspondingVariantChild.setDVariantNumber(newVariantNumber);
                currentChild.setDProductNumber(newProductNumber);
                currentChild.setDVariantNumber(newVariantNumber);
            } else {
                currentChild.setDVariantNumber(currentChild.getInput() - currentChild.getOutput());
                currentChild.setDProductNumber(currentChild.getInput() - currentChild.getOutput());
            }
            if (currentChild.getDVariantNumber() < 0 || currentChild.getDProductNumber() < 0) {
                throw new InsufficientStockException("Stock cannot be negative for product or variant."+currentChild.getDVariantNumber()+","+currentChild.getDProductNumber());
            }
            currentChild.setIdMv(stockParent.getId());
            currentChild.setCreatedAt(LocalDateTime.now());
            super.stockChildRepository.save(currentChild);
        }
        return stockParent;
    }
}
