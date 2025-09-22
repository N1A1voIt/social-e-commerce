package com.itu.socialcom.demo.messages.dtol;

import lombok.Data;

import java.util.List;

@Data
public class OrderPreviewCpl {
    List<VariantWithQuantity> variants;
    String customerName;
    String customerNumber;
    String shippingAddress;
}
