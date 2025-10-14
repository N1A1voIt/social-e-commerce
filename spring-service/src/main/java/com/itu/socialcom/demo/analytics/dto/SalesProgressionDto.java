package com.itu.socialcom.demo.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesProgressionDto {
    private List<String> labels; // Dates in string format: e.g. "2025-03-01"
    private List<Double> data;   // Corresponding sales totals
}
