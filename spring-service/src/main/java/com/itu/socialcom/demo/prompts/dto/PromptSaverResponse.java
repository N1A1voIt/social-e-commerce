package com.itu.socialcom.demo.prompts.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for PromptSaver entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptSaverResponse {
    private Long id;
    private String prompt;
    private Long sellerId;
    private Long platformId;
    private String platformLabel;
    private LocalDateTime createdAt;
}
