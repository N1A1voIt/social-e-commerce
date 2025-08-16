package com.itu.socialcom.demo.products.validation;

import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.OptionValue;
import com.itu.socialcom.demo.products.repository.OptionRepository;
import com.itu.socialcom.demo.products.repository.OptionValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Validator for option value operations in variant creation.
 * Validates that option value IDs exist, belong to the specified product,
 * and that each product option has exactly one value selected.
 * 
 * Requirements addressed:
 * - 1.2: Validate that all selected option values belong to the product's defined options
 * - 6.2: Validate that option values exist and belong to the product
 */
@Component
public class OptionValueValidator {
    
    @Autowired
    private OptionValueRepository optionValueRepository;
    
    @Autowired
    private OptionRepository optionRepository;
    
    /**
     * Validates a list of option value IDs for a specific product.
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs to validate
     * @return ValidationResult containing validation status and error details
     * 
     * Requirements:
     * - 1.2: WHEN a seller creates a variant THEN the system SHALL validate that all selected option values belong to the product's defined options
     * - 6.2: WHEN creating variants with option values THEN the system SHALL validate that option values exist and belong to the product
     */
    public ValidationResult validateOptionValues(Long productId, List<Long> optionValueIds) {
        ValidationResult result = new ValidationResult();
        
        if (optionValueIds == null || optionValueIds.isEmpty()) {
            result.addError("optionValueIds", "At least one option value must be selected");
            return result;
        }
        
        // Remove duplicates
        List<Long> uniqueOptionValueIds = optionValueIds.stream().distinct().collect(Collectors.toList());
        
        // 1. Validate that all option value IDs exist in database
        List<OptionValue> optionValues = optionValueRepository.findAllById(uniqueOptionValueIds);
        Set<Long> foundOptionValueIds = optionValues.stream()
                .map(OptionValue::getIdOv)
                .collect(Collectors.toSet());
        
        for (Long optionValueId : uniqueOptionValueIds) {
            if (!foundOptionValueIds.contains(optionValueId)) {
                result.addError("optionValueIds", "Option value with ID " + optionValueId + " does not exist");
            }
        }
        
        if (!result.isValid()) {
            return result;
        }
        
        // 2. Get all option IDs from the found option values
        Set<Long> optionIds = optionValues.stream()
                .map(OptionValue::getIdOption)
                .collect(Collectors.toSet());
        
        // 3. Validate that all options belong to the specified product
        List<Option> productOptions = optionRepository.findByIdProduct(productId);
        Set<Long> productOptionIds = productOptions.stream()
                .map(Option::getIdOption)
                .collect(Collectors.toSet());
        
        for (Long optionId : optionIds) {
            if (!productOptionIds.contains(optionId)) {
                result.addError("optionValueIds", "Option values contain options that do not belong to this product");
                break;
            }
        }
        
        if (!result.isValid()) {
            return result;
        }
        
        // 4. Validate that each product option has exactly one value selected
        Map<Long, List<OptionValue>> optionValuesByOption = optionValues.stream()
                .collect(Collectors.groupingBy(OptionValue::getIdOption));
        
        for (Option productOption : productOptions) {
            Long optionId = productOption.getIdOption();
            List<OptionValue> valuesForOption = optionValuesByOption.get(optionId);
            
            if (valuesForOption == null || valuesForOption.isEmpty()) {
                result.addError("optionValueIds", "Option '" + productOption.getLabel() + "' must have exactly one value selected");
            } else if (valuesForOption.size() > 1) {
                result.addError("optionValueIds", "Option '" + productOption.getLabel() + "' can only have one value selected, but " + valuesForOption.size() + " were provided");
            }
        }
        
        return result;
    }
    
    /**
     * Validates that option values exist and belong to the product without checking completeness.
     * This is useful for partial validation scenarios.
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs to validate
     * @return ValidationResult containing validation status and error details
     */
    public ValidationResult validateOptionValuesExistence(Long productId, List<Long> optionValueIds) {
        ValidationResult result = new ValidationResult();
        
        if (optionValueIds == null || optionValueIds.isEmpty()) {
            return result; // Empty list is valid for existence check
        }
        
        // Remove duplicates
        List<Long> uniqueOptionValueIds = optionValueIds.stream().distinct().collect(Collectors.toList());
        
        // 1. Validate that all option value IDs exist in database
        List<OptionValue> optionValues = optionValueRepository.findAllById(uniqueOptionValueIds);
        Set<Long> foundOptionValueIds = optionValues.stream()
                .map(OptionValue::getIdOv)
                .collect(Collectors.toSet());
        
        for (Long optionValueId : uniqueOptionValueIds) {
            if (!foundOptionValueIds.contains(optionValueId)) {
                result.addError("optionValueIds", "Option value with ID " + optionValueId + " does not exist");
            }
        }
        
        if (!result.isValid()) {
            return result;
        }
        
        // 2. Validate that all option values belong to the specified product
        Set<Long> optionIds = optionValues.stream()
                .map(OptionValue::getIdOption)
                .collect(Collectors.toSet());
        
        List<Option> productOptions = optionRepository.findByIdProduct(productId);
        Set<Long> productOptionIds = productOptions.stream()
                .map(Option::getIdOption)
                .collect(Collectors.toSet());
        
        for (Long optionId : optionIds) {
            if (!productOptionIds.contains(optionId)) {
                result.addError("optionValueIds", "Option values contain options that do not belong to this product");
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Gets the option values for a product grouped by option.
     * This is useful for generating all possible combinations.
     * 
     * @param productId the ID of the product
     * @return Map of option ID to list of option values
     */
    public Map<Long, List<OptionValue>> getOptionValuesByOption(Long productId) {
        List<Option> productOptions = optionRepository.findByIdProduct(productId);
        Map<Long, List<OptionValue>> result = new HashMap<>();
        
        for (Option option : productOptions) {
            List<OptionValue> optionValues = optionValueRepository.findByIdOption(option.getIdOption());
            result.put(option.getIdOption(), optionValues);
        }
        
        return result;
    }
    
    /**
     * Validation result class to hold validation status and error messages.
     */
    public static class ValidationResult {
        private boolean valid = true;
        private Map<String, List<String>> fieldErrors = new HashMap<>();
        
        public boolean isValid() {
            return valid;
        }
        
        public Map<String, List<String>> getFieldErrors() {
            return fieldErrors;
        }
        
        public void addError(String field, String message) {
            this.valid = false;
            fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
        }
        
        public List<String> getErrors(String field) {
            return fieldErrors.getOrDefault(field, new ArrayList<>());
        }
        
        public boolean hasErrors(String field) {
            return fieldErrors.containsKey(field) && !fieldErrors.get(field).isEmpty();
        }
        
        public String getFirstError(String field) {
            List<String> errors = getErrors(field);
            return errors.isEmpty() ? null : errors.get(0);
        }
        
        public List<String> getAllErrorMessages() {
            return fieldErrors.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
    }
}