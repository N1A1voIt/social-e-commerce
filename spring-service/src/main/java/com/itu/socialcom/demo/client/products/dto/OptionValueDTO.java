package com.itu.socialcom.demo.client.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for option values to be returned to the client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionValueDTO {
    private Long idOv;
    private String value;
}