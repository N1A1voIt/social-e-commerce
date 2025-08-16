package com.itu.socialcom.demo.stocks.dto;

import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.variants.model.Variant;
import lombok.Data;

import java.util.List;

@Data
public class StockUtilities {
    List<Product> products;
    List<Variant> variants;
}
