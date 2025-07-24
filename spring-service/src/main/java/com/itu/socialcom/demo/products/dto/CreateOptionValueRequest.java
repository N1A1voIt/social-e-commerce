package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for creating option values
 */
@Data
public class CreateOptionValueRequest {
    
    @NotBlank(message = "Option value cannot be blank")
    @JsonProperty("value")
    private String value;
    
    /**
     * Default constructor
     */
    public CreateOptionValueRequest() {}
    
    /**
     * Constructor with value
     */
    public CreateOptionValueRequest(String value) {
        this.value = value;
    }
}