package com.itu.socialcom.demo.prompts.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Simple DTO for platform information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformInfo {
    private Long id;
    private String label;
}
