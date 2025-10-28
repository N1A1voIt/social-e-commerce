package com.itu.socialcom.demo.prompts.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating/updating PromptSaver
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptSaverRequest {

    @NotBlank(message = "Prompt cannot be blank")
    private String prompt;

    @NotNull(message = "Platform ID is required")
    private Long platformId;
}
