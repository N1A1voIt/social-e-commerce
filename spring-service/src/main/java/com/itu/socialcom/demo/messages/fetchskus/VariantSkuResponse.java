package com.itu.socialcom.demo.messages.fetchskus;

import lombok.Data;

import java.util.List;
@Data
public class VariantSkuResponse {
    private List<VariantSkuQty> variants;
    String customerName;
    String customerNumber;
    String shippingAddress;
}
