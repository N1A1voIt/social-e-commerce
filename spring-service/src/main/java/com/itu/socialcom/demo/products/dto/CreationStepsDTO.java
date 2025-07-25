package com.itu.socialcom.demo.products.dto;

import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.model.TempProduct;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreationStepsDTO {
    String sessionId;
    TempProduct step1;
    List<OptionValueDTO> step2;
}
