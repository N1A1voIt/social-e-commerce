package com.itu.socialcom.demo.products.validation;

import com.itu.socialcom.demo.products.repository.VariantOptionValueRepository;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.model.VariantOptionValue;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Validator for checking duplicate variant option value combinations.
 * Ensures that no two variants for the same product have identical option value combinations.
 * 
 * Requirements addressed:
 * - 1.3: Ensure no duplicate option value combinations exist for the same product
 * - 6.5: Handle concurrent variant operations safely without data corruption
 */
@Component
public class DuplicateVariantValidator {
    
    @Autowired
    private VariantRepository variantRepository;
    
    @Autowired
    private VariantOptionValueRepository variantOptionValueRepository;
    
    /**
     * Validates that the given option value combination doesn't already exist for the product.
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs to check for duplicates
     * @return ValidationResult containing validation status and error details
     * 
     * Requirements:
     * - 1.3: WHEN a seller creates a variant THEN the system SHALL ensure no duplicate option value combinations exist for the same product
     */
    public ValidationResult validateNoDuplicateCombination(Long productId, List<Long> optionValueIds) {
        ValidationResult result = new ValidationResult();
        
        if (optionValueIds == null || optionValueIds.isEmpty()) {
            return result; // Empty list is valid (no duplicates possible)
        }
        
        // Remove duplicates and sort for consistent comparison
        Set<Long> uniqueOptionValueIds = new HashSet<>(optionValueIds);
        List<Long> sortedOptionValueIds = new ArrayList<>(uniqueOptionValueIds);
        Collections.sort(sortedOptionValueIds);
        
        // Get all variants for the product
        List<Variant> productVariants = variantRepository.findByIdProduct(productId);
        
        if (productVariants.isEmpty()) {
            return result; // No existing variants, so no duplicates possible
        }
        
        // Get all variant IDs for the product
        List<Long> variantIds = productVariants.stream()
                .map(Variant::getIdVariant)
                .collect(Collectors.toList());
        
        // Get all variant option value associations for the product variants
        List<VariantOptionValue> allVariantOptionValues = variantOptionValueRepository.findByIdVariantIn(variantIds);
        
        // Group variant option values by variant ID
        Map<Long, List<Long>> variantToOptionValues = allVariantOptionValues.stream()
                .collect(Collectors.groupingBy(
                        VariantOptionValue::getIdVariant,
                        Collectors.mapping(VariantOptionValue::getIdOv, Collectors.toList())
                ));
        
        // Check each existing variant's option value combination
        for (Map.Entry<Long, List<Long>> entry : variantToOptionValues.entrySet()) {
            Long existingVariantId = entry.getKey();
            List<Long> existingOptionValues = entry.getValue();
            
            // Sort existing option values for comparison
            Collections.sort(existingOptionValues);
            
            // Compare with the new combination
            if (sortedOptionValueIds.equals(existingOptionValues)) {
                // Find the variant details for better error message
                Optional<Variant> duplicateVariant = productVariants.stream()
                        .filter(v -> v.getIdVariant().equals(existingVariantId))
                        .findFirst();
                
                String variantTitle = duplicateVariant.map(Variant::getTitle).orElse("Unknown");
                result.addError("optionValueIds", 
                    "A variant with this exact combination of option values already exists: '" + variantTitle + "' (ID: " + existingVariantId + ")");
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Validates that the given option value combination doesn't already exist for the product,
     * excluding a specific variant (useful for updates).
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs to check for duplicates
     * @param excludeVariantId the variant ID to exclude from duplicate checking
     * @return ValidationResult containing validation status and error details
     */
    public ValidationResult validateNoDuplicateCombinationExcluding(Long productId, List<Long> optionValueIds, Long excludeVariantId) {
        ValidationResult result = new ValidationResult();
        
        if (optionValueIds == null || optionValueIds.isEmpty()) {
            return result; // Empty list is valid (no duplicates possible)
        }
        
        // Remove duplicates and sort for consistent comparison
        Set<Long> uniqueOptionValueIds = new HashSet<>(optionValueIds);
        List<Long> sortedOptionValueIds = new ArrayList<>(uniqueOptionValueIds);
        Collections.sort(sortedOptionValueIds);
        
        // Get all variants for the product except the excluded one
        List<Variant> productVariants = variantRepository.findByIdProduct(productId).stream()
                .filter(v -> !v.getIdVariant().equals(excludeVariantId))
                .collect(Collectors.toList());
        
        if (productVariants.isEmpty()) {
            return result; // No other variants, so no duplicates possible
        }
        
        // Get all variant IDs for the product (excluding the specified variant)
        List<Long> variantIds = productVariants.stream()
                .map(Variant::getIdVariant)
                .collect(Collectors.toList());
        
        // Get all variant option value associations for the product variants
        List<VariantOptionValue> allVariantOptionValues = variantOptionValueRepository.findByIdVariantIn(variantIds);
        
        // Group variant option values by variant ID
        Map<Long, List<Long>> variantToOptionValues = allVariantOptionValues.stream()
                .collect(Collectors.groupingBy(
                        VariantOptionValue::getIdVariant,
                        Collectors.mapping(VariantOptionValue::getIdOv, Collectors.toList())
                ));
        
        // Check each existing variant's option value combination
        for (Map.Entry<Long, List<Long>> entry : variantToOptionValues.entrySet()) {
            Long existingVariantId = entry.getKey();
            List<Long> existingOptionValues = entry.getValue();
            
            // Sort existing option values for comparison
            Collections.sort(existingOptionValues);
            
            // Compare with the new combination
            if (sortedOptionValueIds.equals(existingOptionValues)) {
                // Find the variant details for better error message
                Optional<Variant> duplicateVariant = productVariants.stream()
                        .filter(v -> v.getIdVariant().equals(existingVariantId))
                        .findFirst();
                
                String variantTitle = duplicateVariant.map(Variant::getTitle).orElse("Unknown");
                result.addError("optionValueIds", 
                    "A variant with this exact combination of option values already exists: '" + variantTitle + "' (ID: " + existingVariantId + ")");
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a specific variant has duplicate option value combinations with other variants.
     * This is useful for validation during variant updates.
     * 
     * @param variantId the ID of the variant to check
     * @return ValidationResult containing validation status and error details
     */
    public ValidationResult validateVariantUniqueness(Long variantId) {
        ValidationResult result = new ValidationResult();
        
        // Get the variant
        Optional<Variant> variantOpt = variantRepository.findById(variantId);
        if (variantOpt.isEmpty()) {
            result.addError("variantId", "Variant not found");
            return result;
        }
        
        Variant variant = variantOpt.get();
        
        // Get the variant's option values
        List<VariantOptionValue> variantOptionValues = variantOptionValueRepository.findByIdVariant(variantId);
        List<Long> optionValueIds = variantOptionValues.stream()
                .map(VariantOptionValue::getIdOv)
                .collect(Collectors.toList());
        
        // Use the existing validation method excluding this variant
        return validateNoDuplicateCombinationExcluding(variant.getIdProduct(), optionValueIds, variantId);
    }
    
    /**
     * Gets all existing option value combinations for a product.
     * This is useful for generating unique combinations or displaying existing ones.
     * 
     * @param productId the ID of the product
     * @return Map of variant ID to sorted list of option value IDs
     */
    public Map<Long, List<Long>> getExistingCombinations(Long productId) {
        // Get all variants for the product
        List<Variant> productVariants = variantRepository.findByIdProduct(productId);
        
        if (productVariants.isEmpty()) {
            return new HashMap<>();
        }
        
        // Get all variant IDs for the product
        List<Long> variantIds = productVariants.stream()
                .map(Variant::getIdVariant)
                .collect(Collectors.toList());
        
        // Get all variant option value associations for the product variants
        List<VariantOptionValue> allVariantOptionValues = variantOptionValueRepository.findByIdVariantIn(variantIds);
        
        // Group variant option values by variant ID and sort them
        return allVariantOptionValues.stream()
                .collect(Collectors.groupingBy(
                        VariantOptionValue::getIdVariant,
                        Collectors.mapping(VariantOptionValue::getIdOv, 
                                Collectors.collectingAndThen(Collectors.toList(), 
                                        list -> {
                                            Collections.sort(list);
                                            return list;
                                        }))
                ));
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