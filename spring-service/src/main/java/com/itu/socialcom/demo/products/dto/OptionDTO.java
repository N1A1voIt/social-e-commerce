package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for option data transfer with nested option values
 */
@Data
public class OptionDTO {
    
    @JsonProperty("id")
    private Long idOption;
    
    @JsonProperty("label")
    private String label;
    
    @JsonProperty("productId")
    private Long idProduct;
    
    @JsonProperty("values")
    private List<OptionValueDTO> values;
    
    /**
     * Default constructor
     */
    public OptionDTO() {}
    
    /**
     * Constructor with basic fields
     */
    public OptionDTO(Long idOption, String label, Long idProduct) {
        this.idOption = idOption;
        this.label = label;
        this.idProduct = idProduct;
    }
    
    /**
     * Constructor with all fields
     */
    public OptionDTO(Long idOption, String label, Long idProduct, List<OptionValueDTO> values) {
        this.idOption = idOption;
        this.label = label;
        this.idProduct = idProduct;
        this.values = values;
    }
}