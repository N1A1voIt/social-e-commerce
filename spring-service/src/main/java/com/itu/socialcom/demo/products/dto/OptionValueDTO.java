package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for option value data transfer
 */
@Data
public class OptionValueDTO {
    
    @JsonProperty("id")
    private String idOv;
    
    @JsonProperty("value")
    private String value;
    
    @JsonProperty("optionId")
    private Long idOption;
    
    /**
     * Default constructor
     */
    public OptionValueDTO() {}
    
    /**
     * Constructor with all fields
     */
    public OptionValueDTO(String idOv, String value, Long idOption) {
        this.idOv = idOv;
        this.value = value;
        this.idOption = idOption;
    }
}