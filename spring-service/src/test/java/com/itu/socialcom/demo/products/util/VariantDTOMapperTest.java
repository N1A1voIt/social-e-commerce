package com.itu.socialcom.demo.products.util;

import com.itu.socialcom.demo.products.dto.VariantOptionDTO;
import com.itu.socialcom.demo.products.dto.VariantWithOptionsDTO;
import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.OptionValue;
import com.itu.socialcom.demo.products.repository.OptionRepository;
import com.itu.socialcom.demo.products.repository.OptionValueRepository;
import com.itu.socialcom.demo.products.repository.VariantOptionValueRepository;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.model.VariantInStock;
import com.itu.socialcom.demo.products.variants.model.VariantOptionValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Unit tests for VariantDTOMapper utility class.
 * Tests the mapping functionality and null handling.
 */
@ExtendWith(MockitoExtension.class)
class VariantDTOMapperTest {
    
    @Mock
    private VariantOptionValueRepository variantOptionValueRepository;
    
    @Mock
    private OptionValueRepository optionValueRepository;
    
    @Mock
    private OptionRepository optionRepository;
    
    @InjectMocks
    private VariantDTOMapper variantDTOMapper;
    
    private Variant testVariant;
    private VariantInStock testVariantInStock;
    private VariantOptionValue testVariantOptionValue;
    private OptionValue testOptionValue;
    private Option testOption;
    
    @BeforeEach
    void setUp() {
        // Set up test data
        testVariant = new Variant();
        testVariant.setIdVariant(1L);
        testVariant.setTitle("Test Variant");
        testVariant.setPrice(new BigDecimal("29.99"));
        testVariant.setIdProduct(100L);
        testVariant.setCreatedAt(LocalDateTime.now());
        testVariant.setUpdatedAt(LocalDateTime.now());
        
        testVariantInStock = new VariantInStock();
        // Note: VariantInStock doesn't have setters, so we'll mock the getters in tests
        
        testVariantOptionValue = new VariantOptionValue();
        testVariantOptionValue.setId(1L);
        testVariantOptionValue.setIdVariant(1L);
        testVariantOptionValue.setIdOv(10L);
        
        testOptionValue = new OptionValue();
        testOptionValue.setIdOv(10L);
        testOptionValue.setValue("Red");
        testOptionValue.setIdOption(5L);
        
        testOption = new Option();
        testOption.setIdOption(5L);
        testOption.setLabel("Color");
        testOption.setIdProduct(100L);
    }
    
    @Test
    void testMapToDTO_WithValidVariant_ShouldReturnDTO() {
        // Arrange
        when(variantOptionValueRepository.findByIdVariant(1L))
            .thenReturn(Arrays.asList(testVariantOptionValue));
        when(optionValueRepository.findAllById(anyList()))
            .thenReturn(Arrays.asList(testOptionValue));
        when(optionRepository.findAllById(anyList()))
            .thenReturn(Arrays.asList(testOption));
        
        // Act
        VariantWithOptionsDTO result = variantDTOMapper.mapToDTO(testVariant);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdVariant());
        assertEquals("Test Variant", result.getTitle());
        assertEquals(new BigDecimal("29.99"), result.getPrice());
        assertEquals(100L, result.getIdProduct());
        assertNotNull(result.getOptions());
        assertEquals(1, result.getOptions().size());
        
        VariantOptionDTO optionDTO = result.getOptions().get(0);
        assertEquals(5L, optionDTO.getIdOption());
        assertEquals("Color", optionDTO.getOptionLabel());
        assertEquals(10L, optionDTO.getIdOptionValue());
        assertEquals("Red", optionDTO.getOptionValue());
    }
    
    @Test
    void testMapToDTO_WithNullVariant_ShouldReturnNull() {
        // Act
        VariantWithOptionsDTO result = variantDTOMapper.mapToDTO((Variant) null);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void testMapToDTOList_WithValidVariants_ShouldReturnDTOList() {
        // Arrange
        List<Variant> variants = Arrays.asList(testVariant);
        when(variantOptionValueRepository.findByIdVariantIn(anyList()))
            .thenReturn(Arrays.asList(testVariantOptionValue));
        when(optionValueRepository.findAllById(anyList()))
            .thenReturn(Arrays.asList(testOptionValue));
        when(optionRepository.findAllById(anyList()))
            .thenReturn(Arrays.asList(testOption));
        
        // Act
        List<VariantWithOptionsDTO> result = variantDTOMapper.mapToDTOList(variants);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        VariantWithOptionsDTO dto = result.get(0);
        assertEquals(1L, dto.getIdVariant());
        assertEquals("Test Variant", dto.getTitle());
        assertNotNull(dto.getOptions());
        assertEquals(1, dto.getOptions().size());
    }
    
    @Test
    void testMapToDTOList_WithEmptyList_ShouldReturnEmptyList() {
        // Act
        List<VariantWithOptionsDTO> result = variantDTOMapper.mapToDTOList(Collections.emptyList());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testMapToDTOList_WithNullList_ShouldReturnEmptyList() {
        // Act
        List<VariantWithOptionsDTO> result = variantDTOMapper.mapToDTOList(null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testMapToDTOWithoutOptions_WithValidVariant_ShouldReturnDTOWithEmptyOptions() {
        // Act
        VariantWithOptionsDTO result = variantDTOMapper.mapToDTOWithoutOptions(testVariant);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdVariant());
        assertEquals("Test Variant", result.getTitle());
        assertEquals(new BigDecimal("29.99"), result.getPrice());
        assertEquals(100L, result.getIdProduct());
        assertNotNull(result.getOptions());
        assertTrue(result.getOptions().isEmpty());
    }
    
    @Test
    void testMapToDTOWithoutOptions_WithNullVariant_ShouldReturnNull() {
        // Act
        VariantWithOptionsDTO result = variantDTOMapper.mapToDTOWithoutOptions((Variant) null);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void testMapToDTO_WithNoOptions_ShouldReturnDTOWithEmptyOptionsList() {
        // Arrange
        when(variantOptionValueRepository.findByIdVariant(1L))
            .thenReturn(Collections.emptyList());
        
        // Act
        VariantWithOptionsDTO result = variantDTOMapper.mapToDTO(testVariant);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdVariant());
        assertNotNull(result.getOptions());
        assertTrue(result.getOptions().isEmpty());
    }
    
    @Test
    void testMapToDTOList_WithVariantsContainingNulls_ShouldFilterNulls() {
        // Arrange
        List<Variant> variants = Arrays.asList(testVariant, null, testVariant);
        when(variantOptionValueRepository.findByIdVariantIn(anyList()))
            .thenReturn(Arrays.asList(testVariantOptionValue));
        when(optionValueRepository.findAllById(anyList()))
            .thenReturn(Arrays.asList(testOptionValue));
        when(optionRepository.findAllById(anyList()))
            .thenReturn(Arrays.asList(testOption));
        
        // Act
        List<VariantWithOptionsDTO> result = variantDTOMapper.mapToDTOList(variants);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Should filter out the null variant
        result.forEach(dto -> {
            assertNotNull(dto);
            assertEquals(1L, dto.getIdVariant());
        });
    }
}