package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for variant option information
 */
@Data
public class VariantOptionDTO {
    
    @JsonProperty("idOption")
    private Long idOption;
    
    @JsonProperty("optionLabel")
    private String optionLabel;
    
    @JsonProperty("idOptionValue")
    private Long idOptionValue;
    
    @JsonProperty("optionValue")
    private String optionValue;
    
    /**
     * Default constructor
     */
    public VariantOptionDTO() {}
    
    /**
     * Constructor with all fields
     */
    public VariantOptionDTO(Long idOption, String optionLabel, Long idOptionValue, String optionValue) {
        this.idOption = idOption;
        this.optionLabel = optionLabel;
        this.idOptionValue = idOptionValue;
        this.optionValue = optionValue;
    }
}