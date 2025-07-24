package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for option value data transfer
 */
@Data
public class OptionValueDTO {
    String optionLabels;
    List<String> values;
}