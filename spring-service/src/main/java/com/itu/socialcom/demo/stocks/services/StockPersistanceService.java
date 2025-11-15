package com.itu.socialcom.demo.stocks.services;

import com.itu.socialcom.demo.stocks.InsufficientStockException;
import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockPersistanceService extends StockSavingService {
    private static final Logger log = LoggerFactory.getLogger(StockPersistanceService.class);

    @Override
    @Transactional
    public StockParent saveStock(StockParent stockParent) throws InsufficientStockException {
        stockParentRepository.save(stockParent);

        List<StockChild> items = stockParent.getItems();
        if (items == null || items.isEmpty()) {
            return stockParent;
        }

        // Extract IDs in a single pass (ignore nulls)
        Set<Long> variantIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        for (StockChild item : items) {
            if (item.getIdVariant() != null) variantIds.add(item.getIdVariant());
            if (item.getIdProduct() != null) productIds.add(item.getIdProduct());
        }

        // Fetch last known stock records for referenced variants/products
        Map<Long, StockChild> variantLastRecords = variantIds.isEmpty() ? Collections.emptyMap() :
                stockChildRepository
                        .findMostRecentVariantsByVariantIds(new ArrayList<>(variantIds))
                        .stream()
                        .collect(Collectors.toMap(
                                StockChild::getIdVariant,
                                sc -> sc,
                                (existing, replacement) -> {
                                    // Keep the most recent record (latest createdAt/actionAt)
                                    LocalDateTime existingTime = existing.getActionAt() != null ? existing.getActionAt() : existing.getCreatedAt();
                                    LocalDateTime replacementTime = replacement.getActionAt() != null ? replacement.getActionAt() : replacement.getCreatedAt();
                                    return (replacementTime != null && (existingTime == null || replacementTime.isAfter(existingTime))) 
                                            ? replacement : existing;
                                }
                        ));

        Map<Long, StockChild> productLastRecords = productIds.isEmpty() ? Collections.emptyMap() :
                stockChildRepository
                        .findByLastProductRecords(new ArrayList<>(productIds))
                        .stream()
                        .collect(Collectors.toMap(
                                StockChild::getIdProduct,
                                sc -> sc,
                                (existing, replacement) -> {
                                    // Keep the most recent record (latest createdAt/actionAt)
                                    LocalDateTime existingTime = existing.getActionAt() != null ? existing.getActionAt() : existing.getCreatedAt();
                                    LocalDateTime replacementTime = replacement.getActionAt() != null ? replacement.getActionAt() : replacement.getCreatedAt();
                                    return (replacementTime != null && (existingTime == null || replacementTime.isAfter(existingTime))) 
                                            ? replacement : existing;
                                }
                        ));

        // Seed running totals
        Map<Long, Double> variantTotals = new HashMap<>();
        for (Map.Entry<Long, StockChild> e : variantLastRecords.entrySet()) {
            variantTotals.put(e.getKey(), safeNumber(e.getValue().getDVariantNumber()));
        }
        Map<Long, Double> productTotals = new HashMap<>();
        for (Map.Entry<Long, StockChild> e : productLastRecords.entrySet()) {
            productTotals.put(e.getKey(), safeNumber(e.getValue().getDProductNumber()));
        }

        // Process each item and validate stock levels
        List<StockChild> itemsToSave = new ArrayList<>(items.size());
        for (StockChild currentChild : items) {
            processStockChild(currentChild, variantTotals, productTotals, stockParent.getId());
            validateStockLevels(currentChild);
            itemsToSave.add(currentChild);

            if (log.isDebugEnabled()) {
                log.debug("Stock after movement - Product[{}:{}] = {}, Variant[{}:{}] = {}",
                        currentChild.getIdProduct(), currentChild.getProductName(), currentChild.getDProductNumber(),
                        currentChild.getIdVariant(), currentChild.getVariantName(), currentChild.getDVariantNumber());
            }
        }

        // Batch save all items
        stockChildRepository.saveAll(itemsToSave);

        return stockParent;
    }

    private void processStockChild(StockChild currentChild,
                                   Map<Long, Double> variantTotals,
                                   Map<Long, Double> productTotals,
                                   Long parentId) {
        Long variantId = currentChild.getIdVariant();
        Long productId = currentChild.getIdProduct();

        double delta = safeNumber(currentChild.getInput()) - safeNumber(currentChild.getOutput());

        // Determine previous totals (default 0 if new)
        double prevVariant = variantId == null ? 0.0 : safeNumber(variantTotals.getOrDefault(variantId, 0.0));
        double prevProduct = productId == null ? 0.0 : safeNumber(productTotals.getOrDefault(productId, 0.0));

        double newVariant = prevVariant + delta;
        double newProduct = prevProduct + delta;

        // Update running totals
        if (variantId != null) variantTotals.put(variantId, newVariant);
        if (productId != null) productTotals.put(productId, newProduct);

        // Apply computed balances to current movement row
        currentChild.setDVariantNumber(newVariant);
        currentChild.setDProductNumber(newProduct);

        currentChild.setIdMv(parentId);
        currentChild.setCreatedAt(LocalDateTime.now());
    }

    private void validateStockLevels(StockChild stockChild) throws InsufficientStockException {
        if (stockChild.getDVariantNumber() < 0 || stockChild.getDProductNumber() < 0) {
            String errorMsg = String.format(
                    "Insufficient stock for variant '%s'. Variant stock: %.2f, Product stock: %.2f",
                    stockChild.getVariantName(),
                    stockChild.getDVariantNumber(),
                    stockChild.getDProductNumber()
            );
            throw new InsufficientStockException(errorMsg);
        }
    }

    private double safeNumber(Double v) {
        return v == null ? 0.0 : v;
    }
}
