package com.itu.socialcom.demo.products.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating product options with values
 */
@Data
public class CreateOptionRequest {
    
    @NotBlank(message = "Option label cannot be blank")
    @JsonProperty("label")
    private String label;
    
    @NotEmpty(message = "Option must have at least one value")
    @Valid
    @JsonProperty("values")
    private List<CreateOptionValueRequest> values;
    
    /**
     * Default constructor
     */
    public CreateOptionRequest() {}
    
    /**
     * Constructor with all fields
     */
    public CreateOptionRequest(String label, List<CreateOptionValueRequest> values) {
        this.label = label;
        this.values = values;
    }
}