package com.itu.socialcom.demo.sales.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for CSV import operation.
 * Contains statistics and error information about the import process.
 */
@Data
public class ImportResult {
    
    private int totalRows;
    private int successfulImports;
    private int failedImports;
    private int salesCreated;
    private int salesDetailsCreated;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    
    public void addError(String error) {
        this.errors.add(error);
        this.failedImports++;
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}

