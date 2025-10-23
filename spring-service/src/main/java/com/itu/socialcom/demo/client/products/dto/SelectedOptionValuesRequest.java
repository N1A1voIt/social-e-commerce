package com.itu.socialcom.demo.client.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for selected option values to find a variant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectedOptionValuesRequest {
    private Long productId;
    private List<Long> optionValueIds;
}