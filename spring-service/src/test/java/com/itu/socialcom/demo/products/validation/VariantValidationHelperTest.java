package com.itu.socialcom.demo.products.validation;

import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.OptionValue;
import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.repository.OptionRepository;
import com.itu.socialcom.demo.products.repository.OptionValueRepository;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.repository.VariantOptionValueRepository;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VariantValidationHelper.
 * Tests the centralized validation logic for variant operations.
 */
@ExtendWith(MockitoExtension.class)
class VariantValidationHelperTest {
    
    @Mock
    private OptionValueValidator optionValueValidator;
    
    @Mock
    private DuplicateVariantValidator duplicateVariantValidator;
    
    @Mock
    private SellerOwnershipValidator sellerOwnershipValidator;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private OptionRepository optionRepository;
    
    @Mock
    private OptionValueRepository optionValueRepository;
    
    @Mock
    private VariantRepository variantRepository;
    
    @Mock
    private VariantOptionValueRepository variantOptionValueRepository;
    
    @InjectMocks
    private VariantValidationHelper validationHelper;
    
    private Product testProduct;
    private Option testOption;
    private OptionValue testOptionValue;
    private List<Long> testOptionValueIds;
    
    @BeforeEach
    void setUp() {
        testProduct = new Product("Test Product", BigDecimal.valueOf(100.00), 1L);
        testProduct.setIdProduct(1L);
        
        testOption = new Option();
        testOption.setIdOption(1L);
        testOption.setIdProduct(1L);
        testOption.setLabel("Color");
        
        testOptionValue = new OptionValue();
        testOptionValue.setIdOv(1L);
        testOptionValue.setIdOption(1L);
        testOptionValue.setValue("Red");
        
        testOptionValueIds = Arrays.asList(1L);
    }
    
    @Test
    void testValidateVariantCreation_Success() {
        // Arrange
        Long productId = 1L;
        Integer sellerId = 1;
        
        // Mock successful validations
        doNothing().when(sellerOwnershipValidator).validateProductOwnership(productId, sellerId);
        
        OptionValueValidator.ValidationResult optionValidation = new OptionValueValidator.ValidationResult();
        when(optionValueValidator.validateOptionValues(productId, testOptionValueIds))
            .thenReturn(optionValidation);
        
        DuplicateVariantValidator.ValidationResult duplicateValidation = new DuplicateVariantValidator.ValidationResult();
        when(duplicateVariantValidator.validateNoDuplicateCombination(productId, testOptionValueIds))
            .thenReturn(duplicateValidation);
        
        // Act
        VariantValidationHelper.ValidationResult result = 
            validationHelper.validateVariantCreation(productId, testOptionValueIds, sellerId);
        
        // Assert
        assertTrue(result.isValid());
        assertTrue(result.getFieldErrors().isEmpty());
        verify(sellerOwnershipValidator).validateProductOwnership(productId, sellerId);
        verify(optionValueValidator).validateOptionValues(productId, testOptionValueIds);
        verify(duplicateVariantValidator).validateNoDuplicateCombination(productId, testOptionValueIds);
    }
    
    @Test
    void testValidateVariantCreation_UnauthorizedSeller() {
        // Arrange
        Long productId = 1L;
        Integer sellerId = 1;
        
        doThrow(new SellerOwnershipValidator.UnauthorizedVariantAccessException("Unauthorized"))
            .when(sellerOwnershipValidator).validateProductOwnership(productId, sellerId);
        
        // Act
        VariantValidationHelper.ValidationResult result = 
            validationHelper.validateVariantCreation(productId, testOptionValueIds, sellerId);
        
        // Assert
        assertFalse(result.isValid());
        assertTrue(result.hasErrors("sellerId"));
        assertEquals("Unauthorized", result.getFirstError("sellerId"));
        
        // Should not proceed to other validations
        verify(optionValueValidator, never()).validateOptionValues(any(), anyList());
        verify(duplicateVariantValidator, never()).validateNoDuplicateCombination(any(), anyList());
    }
    
    @Test
    void testValidateVariantCreation_InvalidOptionValues() {
        // Arrange
        Long productId = 1L;
        Integer sellerId = 1;
        
        doNothing().when(sellerOwnershipValidator).validateProductOwnership(productId, sellerId);
        
        OptionValueValidator.ValidationResult optionValidation = new OptionValueValidator.ValidationResult();
        optionValidation.addError("optionValueIds", "Invalid option values");
        when(optionValueValidator.validateOptionValues(productId, testOptionValueIds))
            .thenReturn(optionValidation);
        
        // Act
        VariantValidationHelper.ValidationResult result = 
            validationHelper.validateVariantCreation(productId, testOptionValueIds, sellerId);
        
        // Assert
        assertFalse(result.isValid());
        assertTrue(result.hasErrors("optionValueIds"));
        assertEquals("Invalid option values", result.getFirstError("optionValueIds"));
        
        // Should not proceed to duplicate validation
        verify(duplicateVariantValidator, never()).validateNoDuplicateCombination(any(), anyList());
    }
    
    @Test
    void testValidateVariantGeneration_Success() {
        // Arrange
        Long productId = 1L;
        Integer sellerId = 1;
        
        doNothing().when(sellerOwnershipValidator).validateProductOwnership(productId, sellerId);
        when(optionRepository.findByIdProduct(productId)).thenReturn(Arrays.asList(testOption));
        when(optionValueRepository.findByIdOption(testOption.getIdOption()))
            .thenReturn(Arrays.asList(testOptionValue));
        
        // Act
        VariantValidationHelper.ValidationResult result = 
            validationHelper.validateVariantGeneration(productId, sellerId);
        
        // Assert
        assertTrue(result.isValid());
        assertTrue(result.getFieldErrors().isEmpty());
    }
    
    @Test
    void testValidateVariantGeneration_NoOptions() {
        // Arrange
        Long productId = 1L;
        Integer sellerId = 1;
        
        doNothing().when(sellerOwnershipValidator).validateProductOwnership(productId, sellerId);
        when(optionRepository.findByIdProduct(productId)).thenReturn(Collections.emptyList());
        
        // Act
        VariantValidationHelper.ValidationResult result = 
            validationHelper.validateVariantGeneration(productId, sellerId);
        
        // Assert
        assertFalse(result.isValid());
        assertTrue(result.hasErrors("productId"));
        assertEquals("Product must have at least one option to generate variants", 
            result.getFirstError("productId"));
    }
    
    @Test
    void testValidateBusinessRules_Success() {
        // Arrange
        Long productId = 1L;
        
        when(optionValueRepository.findAllById(testOptionValueIds))
            .thenReturn(Arrays.asList(testOptionValue));
        when(optionRepository.findByIdProduct(productId))
            .thenReturn(Arrays.asList(testOption));
        
        // Act
        VariantValidationHelper.ValidationResult result = 
            validationHelper.validateBusinessRules(productId, testOptionValueIds);
        
        // Assert
        assertTrue(result.isValid());
        assertTrue(result.getFieldErrors().isEmpty());
    }
    
    @Test
    void testValidateBusinessRules_EmptyOptionValues() {
        // Arrange
        Long productId = 1L;
        List<Long> emptyList = Collections.emptyList();
        
        // Act
        VariantValidationHelper.ValidationResult result = 
            validationHelper.validateBusinessRules(productId, emptyList);
        
        // Assert
        assertFalse(result.isValid());
        assertTrue(result.hasErrors("optionValueIds"));
        assertEquals("At least one option value must be selected", 
            result.getFirstError("optionValueIds"));
    }
    
    @Test
    void testIsValidOptionValueCombination_Valid() {
        // Arrange
        Long productId = 1L;
        
        when(optionValueRepository.findAllById(testOptionValueIds))
            .thenReturn(Arrays.asList(testOptionValue));
        when(optionRepository.findByIdProduct(productId))
            .thenReturn(Arrays.asList(testOption));
        
        // Act
        boolean result = validationHelper.isValidOptionValueCombination(productId, testOptionValueIds);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void testIsValidOptionValueCombination_Invalid() {
        // Arrange
        Long productId = 1L;
        
        when(optionValueRepository.findAllById(testOptionValueIds))
            .thenReturn(Collections.emptyList()); // No option values found
        
        // Act
        boolean result = validationHelper.isValidOptionValueCombination(productId, testOptionValueIds);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void testValidationResult_MergeWith() {
        // Arrange
        VariantValidationHelper.ValidationResult result1 = new VariantValidationHelper.ValidationResult();
        result1.addError("field1", "error1");
        result1.addMetadata("key1", "value1");
        
        VariantValidationHelper.ValidationResult result2 = new VariantValidationHelper.ValidationResult();
        result2.addError("field2", "error2");
        result2.addMetadata("key2", "value2");
        
        // Act
        result1.mergeWith(result2);
        
        // Assert
        assertFalse(result1.isValid());
        assertTrue(result1.hasErrors("field1"));
        assertTrue(result1.hasErrors("field2"));
        assertEquals("error1", result1.getFirstError("field1"));
        assertEquals("error2", result1.getFirstError("field2"));
        assertEquals("value1", result1.getMetadata().get("key1"));
        assertEquals("value2", result1.getMetadata().get("key2"));
    }
    
    @Test
    void testValidationResult_StaticMethods() {
        // Test success
        VariantValidationHelper.ValidationResult success = VariantValidationHelper.ValidationResult.success();
        assertTrue(success.isValid());
        assertTrue(success.getFieldErrors().isEmpty());
        
        // Test failure
        VariantValidationHelper.ValidationResult failure = 
            VariantValidationHelper.ValidationResult.failure("field", "message");
        assertFalse(failure.isValid());
        assertTrue(failure.hasErrors("field"));
        assertEquals("message", failure.getFirstError("field"));
    }
    
    @Test
    void testGetValidationDetails() {
        // Arrange
        Long productId = 1L;
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(optionValueRepository.findAllById(testOptionValueIds))
            .thenReturn(Arrays.asList(testOptionValue));
        when(optionRepository.findByIdProduct(productId))
            .thenReturn(Arrays.asList(testOption));
        when(variantRepository.findByIdProduct(productId))
            .thenReturn(Collections.emptyList());
        
        // Act
        var details = validationHelper.getValidationDetails(productId, testOptionValueIds);
        
        // Assert
        assertTrue((Boolean) details.get("productExists"));
        assertEquals("Test Product", details.get("productName"));
        assertEquals(1L, details.get("sellerId"));
        assertEquals(testOptionValueIds, details.get("requestedOptionValueIds"));
        assertEquals(1, details.get("foundOptionValues"));
        assertEquals(0, details.get("existingVariantCount"));
    }
}