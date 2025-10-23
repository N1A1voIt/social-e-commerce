package com.itu.socialcom.demo.client.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for product options to be returned to the client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionDTO {
    private Long idOption;
    private String label;
    private List<OptionValueDTO> optionValues = new ArrayList<>();
}