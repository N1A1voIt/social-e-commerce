package com.itu.socialcom.demo.messages.dtol;

import com.itu.socialcom.demo.products.variants.model.Variant;
import lombok.Data;

@Data
public class VariantWithQuantity {
    Variant variant;
    int quantity;
}
