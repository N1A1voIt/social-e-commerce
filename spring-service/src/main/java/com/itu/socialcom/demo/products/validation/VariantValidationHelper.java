package com.itu.socialcom.demo.products.validation;

import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.OptionValue;
import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.repository.OptionRepository;
import com.itu.socialcom.demo.products.repository.OptionValueRepository;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.repository.VariantOptionValueRepository;
import com.itu.socialcom.demo.products.variants.exception.DuplicateVariantException;
import com.itu.socialcom.demo.products.variants.exception.InvalidOptionValueException;
import com.itu.socialcom.demo.products.variants.exception.UnauthorizedVariantAccessException;
import com.itu.socialcom.demo.products.variants.exception.VariantNotFoundException;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.model.VariantOptionValue;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Centralized validation helper for variant operations.
 * Consolidates validation logic for reuse across service methods and provides
 * structured validation results with comprehensive business rule checking.
 * 
 * Requirements addressed:
 * - 1.2: Validate option value combinations against product options
 * - 1.3: Check business rules and constraints for duplicate prevention
 * - 6.2: Validate that option values exist and belong to the product
 * - 6.5: Handle concurrent variant operations safely without data corruption
 */
@Component
public class VariantValidationHelper {
    
    @Autowired
    private OptionValueValidator optionValueValidator;
    
    @Autowired
    private DuplicateVariantValidator duplicateVariantValidator;
    
    @Autowired
    private SellerOwnershipValidator sellerOwnershipValidator;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OptionRepository optionRepository;
    
    @Autowired
    private OptionValueRepository optionValueRepository;
    
    @Autowired
    private VariantRepository variantRepository;
    
    @Autowired
    private VariantOptionValueRepository variantOptionValueRepository;
    
    /**
     * Performs comprehensive validation for variant creation with option values.
     * This method centralizes all validation logic required for creating a new variant.
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs for the variant
     * @param sellerId the ID of the seller
     * @return ValidationResult containing all validation outcomes
     * 
     * Requirements:
     * - 1.2: WHEN a seller creates a variant THEN the system SHALL validate that all selected option values belong to the product's defined options
     * - 1.3: WHEN a seller creates a variant THEN the system SHALL ensure no duplicate option value combinations exist for the same product
     * - 6.1: WHEN any variant operation is performed THEN the system SHALL validate seller ownership of the parent product
     * - 6.2: WHEN creating variants with option values THEN the system SHALL validate that option values exist and belong to the product
     */
    public ValidationResult validateVariantCreation(Long productId, List<Long> optionValueIds, Integer sellerId) {
        ValidationResult result = new ValidationResult();
        
        // 1. Validate seller ownership
        try {
            sellerOwnershipValidator.validateProductOwnership(productId, sellerId);
        } catch (SellerOwnershipValidator.UnauthorizedVariantAccessException e) {
            result.addError("sellerId", e.getMessage());
            return result; // Stop validation if seller doesn't own product
        }
        
        // 2. Validate option values exist and belong to product
        OptionValueValidator.ValidationResult optionValidation = 
            optionValueValidator.validateOptionValues(productId, optionValueIds);
        result.mergeWith(optionValidation);
        
        if (!result.isValid()) {
            return result; // Stop if option values are invalid
        }
        
        // 3. Check for duplicate variant combinations
        DuplicateVariantValidator.ValidationResult duplicateValidation = 
            duplicateVariantValidator.validateNoDuplicateCombination(productId, optionValueIds);
        result.mergeWith(duplicateValidation);
        
        return result;
    }
    
    /**
     * Performs comprehensive validation for variant updates.
     * This method validates ownership and ensures business rules are maintained during updates.
     * 
     * @param productId the ID of the product
     * @param variantId the ID of the variant to update
     * @param sellerId the ID of the seller
     * @return ValidationResult containing all validation outcomes
     * 
     * Requirements:
     * - 4.4: IF a seller tries to update a variant they don't own THEN the system SHALL reject the request
     * - 6.1: WHEN any variant operation is performed THEN the system SHALL validate seller ownership of the parent product
     */
    public ValidationResult validateVariantUpdate(Long productId, Long variantId, Integer sellerId) {
        ValidationResult result = new ValidationResult();
        
        // 1. Validate seller ownership of product and variant
        try {
            sellerOwnershipValidator.validateProductAndVariantOwnership(productId, variantId, sellerId);
        } catch (SellerOwnershipValidator.UnauthorizedVariantAccessException e) {
            result.addError("sellerId", e.getMessage());
        } catch (SellerOwnershipValidator.VariantNotFoundException e) {
            result.addError("variantId", e.getMessage());
        } catch (IllegalArgumentException e) {
            result.addError("variantId", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Performs comprehensive validation for variant deletion.
     * This method validates ownership before allowing variant deletion.
     * 
     * @param productId the ID of the product
     * @param variantId the ID of the variant to delete
     * @param sellerId the ID of the seller
     * @return ValidationResult containing all validation outcomes
     * 
     * Requirements:
     * - 5.3: IF a seller tries to delete a variant they don't own THEN the system SHALL reject the request
     * - 6.1: WHEN any variant operation is performed THEN the system SHALL validate seller ownership of the parent product
     */
    public ValidationResult validateVariantDeletion(Long productId, Long variantId, Integer sellerId) {
        ValidationResult result = new ValidationResult();
        
        // 1. Validate seller ownership of product and variant
        try {
            sellerOwnershipValidator.validateProductAndVariantOwnership(productId, variantId, sellerId);
        } catch (SellerOwnershipValidator.UnauthorizedVariantAccessException e) {
            result.addError("sellerId", e.getMessage());
        } catch (SellerOwnershipValidator.VariantNotFoundException e) {
            result.addError("variantId", e.getMessage());
        } catch (IllegalArgumentException e) {
            result.addError("variantId", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Validates option value combinations for automatic variant generation.
     * This method ensures that the product has valid options for generating variants.
     * 
     * @param productId the ID of the product
     * @param sellerId the ID of the seller
     * @return ValidationResult containing validation outcomes
     * 
     * Requirements:
     * - 2.4: IF no options exist for a product THEN the system SHALL reject automatic variant generation
     * - 6.1: WHEN any variant operation is performed THEN the system SHALL validate seller ownership of the parent product
     */
    public ValidationResult validateVariantGeneration(Long productId, Integer sellerId) {
        ValidationResult result = new ValidationResult();
        
        // 1. Validate seller ownership
        try {
            sellerOwnershipValidator.validateProductOwnership(productId, sellerId);
        } catch (SellerOwnershipValidator.UnauthorizedVariantAccessException e) {
            result.addError("sellerId", e.getMessage());
            return result; // Stop validation if seller doesn't own product
        }
        
        // 2. Check if product has options
        List<Option> productOptions = optionRepository.findByIdProduct(productId);
        if (productOptions.isEmpty()) {
            result.addError("productId", "Product must have at least one option to generate variants");
            return result;
        }
        
        // 3. Check if all options have at least one value
        for (Option option : productOptions) {
            List<OptionValue> optionValues = optionValueRepository.findByIdOption(option.getIdOption());
            if (optionValues.isEmpty()) {
                result.addError("optionId_" + option.getIdOption(), 
                    "Option '" + option.getLabel() + "' must have at least one value to generate variants");
            }
        }
        
        return result;
    }
    
    /**
     * Validates business rules and constraints for a specific set of option values.
     * This method performs deep validation of option value combinations against business rules.
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs to validate
     * @return ValidationResult containing detailed validation outcomes
     * 
     * Requirements:
     * - 1.2: Validate option value combinations against product options
     * - 6.2: Validate that option values exist and belong to the product
     */
    public ValidationResult validateBusinessRules(Long productId, List<Long> optionValueIds) {
        ValidationResult result = new ValidationResult();
        
        if (optionValueIds == null || optionValueIds.isEmpty()) {
            result.addError("optionValueIds", "At least one option value must be selected");
            return result;
        }
        
        // 1. Validate option values exist
        List<OptionValue> optionValues = optionValueRepository.findAllById(optionValueIds);
        Set<Long> foundOptionValueIds = optionValues.stream()
                .map(OptionValue::getIdOv)
                .collect(Collectors.toSet());
        
        List<Long> missingIds = optionValueIds.stream()
                .filter(id -> !foundOptionValueIds.contains(id))
                .collect(Collectors.toList());
        
        if (!missingIds.isEmpty()) {
            result.addError("optionValueIds", "Option values with IDs " + missingIds + " do not exist");
            return result;
        }
        
        // 2. Validate option values belong to product
        Set<Long> optionIds = optionValues.stream()
                .map(OptionValue::getIdOption)
                .collect(Collectors.toSet());
        
        List<Option> productOptions = optionRepository.findByIdProduct(productId);
        Set<Long> productOptionIds = productOptions.stream()
                .map(Option::getIdOption)
                .collect(Collectors.toSet());
        
        List<Long> invalidOptionIds = optionIds.stream()
                .filter(id -> !productOptionIds.contains(id))
                .collect(Collectors.toList());
        
        if (!invalidOptionIds.isEmpty()) {
            result.addError("optionValueIds", "Option values contain options that do not belong to this product");
            return result;
        }
        
        // 3. Validate completeness - each product option must have exactly one value
        Map<Long, List<OptionValue>> optionValuesByOption = optionValues.stream()
                .collect(Collectors.groupingBy(OptionValue::getIdOption));
        
        for (Option productOption : productOptions) {
            Long optionId = productOption.getIdOption();
            List<OptionValue> valuesForOption = optionValuesByOption.get(optionId);
            
            if (valuesForOption == null || valuesForOption.isEmpty()) {
                result.addError("optionValueIds", 
                    "Option '" + productOption.getLabel() + "' must have exactly one value selected");
            } else if (valuesForOption.size() > 1) {
                result.addError("optionValueIds", 
                    "Option '" + productOption.getLabel() + "' can only have one value selected, but " + 
                    valuesForOption.size() + " were provided");
            }
        }
        
        return result;
    }
    
    /**
     * Validates constraints for concurrent operations to prevent data corruption.
     * This method performs additional checks to ensure thread-safe operations.
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs
     * @param excludeVariantId optional variant ID to exclude from duplicate checking (for updates)
     * @return ValidationResult containing constraint validation outcomes
     * 
     * Requirements:
     * - 6.5: WHEN concurrent variant operations occur THEN the system SHALL handle them safely without data corruption
     */
    public ValidationResult validateConcurrencyConstraints(Long productId, List<Long> optionValueIds, Long excludeVariantId) {
        ValidationResult result = new ValidationResult();
        
        // 1. Re-validate option values exist (they might have been deleted concurrently)
        List<OptionValue> optionValues = optionValueRepository.findAllById(optionValueIds);
        if (optionValues.size() != optionValueIds.size()) {
            result.addError("optionValueIds", "One or more option values were deleted during processing");
            return result;
        }
        
        // 2. Re-validate product exists
        if (!productRepository.existsById(productId)) {
            result.addError("productId", "Product was deleted during processing");
            return result;
        }
        
        // 3. Check for duplicate combinations with exclusion
        DuplicateVariantValidator.ValidationResult duplicateValidation;
        if (excludeVariantId != null) {
            duplicateValidation = duplicateVariantValidator.validateNoDuplicateCombinationExcluding(
                productId, optionValueIds, excludeVariantId);
        } else {
            duplicateValidation = duplicateVariantValidator.validateNoDuplicateCombination(
                productId, optionValueIds);
        }
        
        result.mergeWith(duplicateValidation);
        
        return result;
    }
    
    /**
     * Performs a quick validation check for basic requirements.
     * This method provides a lightweight validation for simple use cases.
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs
     * @return true if basic validation passes, false otherwise
     */
    public boolean isValidOptionValueCombination(Long productId, List<Long> optionValueIds) {
        if (productId == null || optionValueIds == null || optionValueIds.isEmpty()) {
            return false;
        }
        
        // Quick existence check
        List<OptionValue> optionValues = optionValueRepository.findAllById(optionValueIds);
        if (optionValues.size() != optionValueIds.size()) {
            return false;
        }
        
        // Quick product ownership check
        Set<Long> optionIds = optionValues.stream()
                .map(OptionValue::getIdOption)
                .collect(Collectors.toSet());
        
        List<Option> productOptions = optionRepository.findByIdProduct(productId);
        Set<Long> productOptionIds = productOptions.stream()
                .map(Option::getIdOption)
                .collect(Collectors.toSet());
        
        return productOptionIds.containsAll(optionIds);
    }
    
    /**
     * Gets detailed information about validation failures for debugging purposes.
     * This method provides comprehensive error details for troubleshooting.
     * 
     * @param productId the ID of the product
     * @param optionValueIds the list of option value IDs
     * @return Map containing detailed validation information
     */
    public Map<String, Object> getValidationDetails(Long productId, List<Long> optionValueIds) {
        Map<String, Object> details = new HashMap<>();
        
        // Product information
        Optional<Product> product = productRepository.findById(productId);
        details.put("productExists", product.isPresent());
        if (product.isPresent()) {
            details.put("productName", product.get().getName());
            details.put("sellerId", product.get().getIdSeller());
        }
        
        // Option value information
        List<OptionValue> optionValues = optionValueRepository.findAllById(optionValueIds);
        details.put("requestedOptionValueIds", optionValueIds);
        details.put("foundOptionValues", optionValues.size());
        details.put("missingOptionValueIds", 
            optionValueIds.stream()
                .filter(id -> optionValues.stream().noneMatch(ov -> ov.getIdOv().equals(id)))
                .collect(Collectors.toList()));
        
        // Product options information
        List<Option> productOptions = optionRepository.findByIdProduct(productId);
        details.put("productOptions", productOptions.stream()
            .collect(Collectors.toMap(Option::getIdOption, Option::getLabel)));
        
        // Existing variants information
        List<Variant> existingVariants = variantRepository.findByIdProduct(productId);
        details.put("existingVariantCount", existingVariants.size());
        
        return details;
    }
    
    /**
     * Unified validation result class that consolidates all validation outcomes.
     * This class provides structured error handling and detailed validation feedback.
     */
    public static class ValidationResult {
        private boolean valid = true;
        private Map<String, List<String>> fieldErrors = new HashMap<>();
        private Map<String, Object> metadata = new HashMap<>();
        
        public boolean isValid() {
            return valid;
        }
        
        public Map<String, List<String>> getFieldErrors() {
            return new HashMap<>(fieldErrors);
        }
        
        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }
        
        public void addError(String field, String message) {
            this.valid = false;
            fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
        }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
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
        
        public Set<String> getErrorFields() {
            return new HashSet<>(fieldErrors.keySet());
        }
        
        public int getErrorCount() {
            return fieldErrors.values().stream()
                    .mapToInt(List::size)
                    .sum();
        }
        
        /**
         * Merges another ValidationResult into this one.
         * This is useful for combining validation results from multiple validators.
         * 
         * @param other the ValidationResult to merge
         */
        public void mergeWith(ValidationResult other) {
            if (!other.isValid()) {
                this.valid = false;
            }
            
            for (Map.Entry<String, List<String>> entry : other.fieldErrors.entrySet()) {
                String field = entry.getKey();
                List<String> errors = entry.getValue();
                for (String error : errors) {
                    addError(field, error);
                }
            }
            
            this.metadata.putAll(other.metadata);
        }
        
        /**
         * Merges with OptionValueValidator.ValidationResult.
         */
        public void mergeWith(OptionValueValidator.ValidationResult other) {
            if (!other.isValid()) {
                this.valid = false;
            }
            
            for (Map.Entry<String, List<String>> entry : other.getFieldErrors().entrySet()) {
                String field = entry.getKey();
                List<String> errors = entry.getValue();
                for (String error : errors) {
                    addError(field, error);
                }
            }
        }
        
        /**
         * Merges with DuplicateVariantValidator.ValidationResult.
         */
        public void mergeWith(DuplicateVariantValidator.ValidationResult other) {
            if (!other.isValid()) {
                this.valid = false;
            }
            
            for (Map.Entry<String, List<String>> entry : other.getFieldErrors().entrySet()) {
                String field = entry.getKey();
                List<String> errors = entry.getValue();
                for (String error : errors) {
                    addError(field, error);
                }
            }
        }
        
        /**
         * Creates a ValidationResult from an exception.
         * This is useful for converting exceptions to structured validation results.
         * 
         * @param exception the exception to convert
         * @return ValidationResult representing the exception
         */
        public static ValidationResult fromException(Exception exception) {
            ValidationResult result = new ValidationResult();
            
            if (exception instanceof SellerOwnershipValidator.UnauthorizedVariantAccessException) {
                result.addError("sellerId", exception.getMessage());
            } else if (exception instanceof SellerOwnershipValidator.VariantNotFoundException) {
                result.addError("variantId", exception.getMessage());
            } else if (exception instanceof InvalidOptionValueException) {
                InvalidOptionValueException ive = (InvalidOptionValueException) exception;
                for (Map.Entry<String, String> entry : ive.getFieldErrors().entrySet()) {
                    result.addError(entry.getKey(), entry.getValue());
                }
            } else if (exception instanceof DuplicateVariantException) {
                DuplicateVariantException dve = (DuplicateVariantException) exception;
                for (Map.Entry<String, String> entry : dve.getFieldErrors().entrySet()) {
                    result.addError(entry.getKey(), entry.getValue());
                }
            } else {
                result.addError("general", exception.getMessage());
            }
            
            return result;
        }
        
        /**
         * Creates a successful ValidationResult.
         * 
         * @return a valid ValidationResult with no errors
         */
        public static ValidationResult success() {
            return new ValidationResult();
        }
        
        /**
         * Creates a failed ValidationResult with a single error.
         * 
         * @param field the field name
         * @param message the error message
         * @return a ValidationResult with the specified error
         */
        public static ValidationResult failure(String field, String message) {
            ValidationResult result = new ValidationResult();
            result.addError(field, message);
            return result;
        }
    }
}