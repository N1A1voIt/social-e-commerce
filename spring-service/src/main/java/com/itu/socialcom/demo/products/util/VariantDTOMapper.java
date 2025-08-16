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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for mapping Variant entities to VariantWithOptionsDTO.
 * Handles the assembly of option details from multiple repository queries
 * and provides optimized batch mapping operations.
 * 
 * Requirements addressed:
 * - 3.1: Map variants with their associated option values
 * - 3.2: Assemble option details from multiple repository queries
 * - 3.3: Handle null values and missing associations gracefully
 */
@Component
public class VariantDTOMapper {
    
    @Autowired
    private VariantOptionValueRepository variantOptionValueRepository;
    
    @Autowired
    private OptionValueRepository optionValueRepository;
    
    @Autowired
    private OptionRepository optionRepository;
    
    /**
     * Maps a single Variant entity to VariantWithOptionsDTO.
     * Fetches associated option details and handles null values gracefully.
     * 
     * @param variant the variant entity to map
     * @return VariantWithOptionsDTO with option details, or null if variant is null
     * 
     * Requirement 3.1: WHEN displaying variants THEN the system SHALL include option labels and values for each variant
     * Requirement 3.3: Handle null values and missing associations gracefully
     */
    public VariantWithOptionsDTO mapToDTO(Variant variant) {
        if (variant == null) {
            return null;
        }
        
        VariantWithOptionsDTO dto = new VariantWithOptionsDTO();
        dto.setIdVariant(variant.getIdVariant());
        dto.setTitle(variant.getTitle());
        dto.setPrice(variant.getPrice());
        dto.setIdProduct(variant.getIdProduct());
        dto.setCreatedAt(variant.getCreatedAt());
        dto.setUpdatedAt(variant.getUpdatedAt());
        
        // Fetch and map option details
        List<VariantOptionDTO> options = fetchVariantOptions(variant.getIdVariant());
        dto.setOptions(options);
        
        return dto;
    }
    
    /**
     * Maps a single VariantInStock entity to VariantWithOptionsDTO.
     * Includes stock information and fetches associated option details.
     * 
     * @param variantInStock the variant in stock entity to map
     * @return VariantWithOptionsDTO with stock and option details, or null if variantInStock is null
     * 
     * Requirement 3.1: WHEN displaying variants THEN the system SHALL show variant title, price, and creation timestamp
     * Requirement 3.3: Handle null values and missing associations gracefully
     */
    public VariantWithOptionsDTO mapToDTO(VariantInStock variantInStock) {
        if (variantInStock == null) {
            return null;
        }
        
        VariantWithOptionsDTO dto = new VariantWithOptionsDTO();
        dto.setIdVariant(variantInStock.getIdVariant());
        dto.setTitle(variantInStock.getTitle());
        dto.setPrice(variantInStock.getPrice());
        dto.setIdProduct(variantInStock.getIdProduct());
        dto.setCreatedAt(variantInStock.getCreatedAt());
        dto.setUpdatedAt(variantInStock.getUpdatedAt());
        
        // Set stock information
        dto.setStockQuantity(variantInStock.getVariantNumber() != null ? 
            variantInStock.getVariantNumber().intValue() : null);
        dto.setStockStatus(variantInStock.getStockStatus());
        
        // Fetch and map option details
        List<VariantOptionDTO> options = fetchVariantOptions(variantInStock.getIdVariant());
        dto.setOptions(options);
        
        return dto;
    }
    
    /**
     * Maps a list of Variant entities to VariantWithOptionsDTO list.
     * Optimized for batch operations to minimize database queries.
     * 
     * @param variants the list of variant entities to map
     * @return list of VariantWithOptionsDTO with option details
     * 
     * Requirement 3.2: Optimize for batch mapping operations
     * Requirement 3.3: Handle null values and missing associations gracefully
     */
    public List<VariantWithOptionsDTO> mapToDTOList(List<Variant> variants) {
        if (variants == null || variants.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Extract variant IDs for batch queries
        List<Long> variantIds = variants.stream()
            .filter(Objects::nonNull)
            .map(Variant::getIdVariant)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (variantIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Batch fetch all option associations
        Map<Long, List<VariantOptionDTO>> variantOptionsMap = batchFetchVariantOptions(variantIds);
        
        // Map variants to DTOs
        return variants.stream()
            .filter(Objects::nonNull)
            .map(variant -> {
                VariantWithOptionsDTO dto = new VariantWithOptionsDTO();
                dto.setIdVariant(variant.getIdVariant());
                dto.setTitle(variant.getTitle());
                dto.setPrice(variant.getPrice());
                dto.setIdProduct(variant.getIdProduct());
                dto.setCreatedAt(variant.getCreatedAt());
                dto.setUpdatedAt(variant.getUpdatedAt());
                
                // Set options from batch-fetched data
                List<VariantOptionDTO> options = variantOptionsMap.getOrDefault(
                    variant.getIdVariant(), new ArrayList<>());
                dto.setOptions(options);
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Maps a list of VariantInStock entities to VariantWithOptionsDTO list.
     * Optimized for batch operations and includes stock information.
     * 
     * @param variantsInStock the list of variant in stock entities to map
     * @return list of VariantWithOptionsDTO with stock and option details
     * 
     * Requirement 3.2: Optimize for batch mapping operations
     * Requirement 3.3: Handle null values and missing associations gracefully
     */
    public List<VariantWithOptionsDTO> mapVariantsInStockToDTOList(List<VariantInStock> variantsInStock) {
        if (variantsInStock == null || variantsInStock.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Extract variant IDs for batch queries
        List<Long> variantIds = variantsInStock.stream()
            .filter(Objects::nonNull)
            .map(VariantInStock::getIdVariant)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (variantIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Batch fetch all option associations
        Map<Long, List<VariantOptionDTO>> variantOptionsMap = batchFetchVariantOptions(variantIds);
        
        // Map variants to DTOs
        return variantsInStock.stream()
            .filter(Objects::nonNull)
            .map(variantInStock -> {
                VariantWithOptionsDTO dto = new VariantWithOptionsDTO();
                dto.setIdVariant(variantInStock.getIdVariant());
                dto.setTitle(variantInStock.getTitle());
                dto.setPrice(variantInStock.getPrice());
                dto.setIdProduct(variantInStock.getIdProduct());
                dto.setCreatedAt(variantInStock.getCreatedAt());
                dto.setUpdatedAt(variantInStock.getUpdatedAt());
                
                // Set stock information
                dto.setStockQuantity(variantInStock.getVariantNumber() != null ? 
                    variantInStock.getVariantNumber().intValue() : null);
                dto.setStockStatus(variantInStock.getStockStatus());
                
                // Set options from batch-fetched data
                List<VariantOptionDTO> options = variantOptionsMap.getOrDefault(
                    variantInStock.getIdVariant(), new ArrayList<>());
                dto.setOptions(options);
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Fetches option details for a single variant.
     * Handles null values and missing associations gracefully.
     * 
     * @param variantId the ID of the variant
     * @return list of VariantOptionDTO for the variant
     * 
     * Requirement 3.1: Assemble option details from multiple repository queries
     * Requirement 3.3: Handle null values and missing associations gracefully
     */
    private List<VariantOptionDTO> fetchVariantOptions(Long variantId) {
        if (variantId == null) {
            return new ArrayList<>();
        }
        
        try {
            // Fetch variant option value associations
            List<VariantOptionValue> associations = variantOptionValueRepository.findByIdVariant(variantId);
            
            if (associations.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Extract option value IDs
            List<Long> optionValueIds = associations.stream()
                .map(VariantOptionValue::getIdOv)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            if (optionValueIds.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Fetch option values
            List<OptionValue> optionValues = optionValueRepository.findAllById(optionValueIds);
            
            // Create map for quick lookup
            Map<Long, OptionValue> optionValueMap = optionValues.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(OptionValue::getIdOv, ov -> ov, (existing, replacement) -> existing));
            
            // Extract option IDs
            List<Long> optionIds = optionValues.stream()
                .map(OptionValue::getIdOption)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
            
            if (optionIds.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Fetch options
            List<Option> options = optionRepository.findAllById(optionIds);
            
            // Create map for quick lookup
            Map<Long, Option> optionMap = options.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Option::getIdOption, o -> o, (existing, replacement) -> existing));
            
            // Build VariantOptionDTO list
            return associations.stream()
                .filter(Objects::nonNull)
                .map(association -> {
                    OptionValue optionValue = optionValueMap.get(association.getIdOv());
                    if (optionValue == null) {
                        return null;
                    }
                    
                    Option option = optionMap.get(optionValue.getIdOption());
                    if (option == null) {
                        return null;
                    }
                    
                    return new VariantOptionDTO(
                        option.getIdOption(),
                        option.getLabel(),
                        optionValue.getIdOv(),
                        optionValue.getValue()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            // Log error and return empty list to handle gracefully
            System.err.println("Error fetching variant options for variant " + variantId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Batch fetches option details for multiple variants.
     * Optimized to minimize database queries for better performance.
     * 
     * @param variantIds the list of variant IDs
     * @return map of variant ID to list of VariantOptionDTO
     * 
     * Requirement 3.2: Optimize for batch mapping operations
     * Requirement 3.3: Handle null values and missing associations gracefully
     */
    private Map<Long, List<VariantOptionDTO>> batchFetchVariantOptions(List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            // Batch fetch all variant option value associations
            List<VariantOptionValue> allAssociations = variantOptionValueRepository.findByIdVariantIn(variantIds);
            
            if (allAssociations.isEmpty()) {
                return new HashMap<>();
            }
            
            // Extract all option value IDs
            List<Long> allOptionValueIds = allAssociations.stream()
                .map(VariantOptionValue::getIdOv)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
            
            if (allOptionValueIds.isEmpty()) {
                return new HashMap<>();
            }
            
            // Batch fetch all option values
            List<OptionValue> allOptionValues = optionValueRepository.findAllById(allOptionValueIds);
            
            // Create map for quick lookup
            Map<Long, OptionValue> optionValueMap = allOptionValues.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(OptionValue::getIdOv, ov -> ov, (existing, replacement) -> existing));
            
            // Extract all option IDs
            List<Long> allOptionIds = allOptionValues.stream()
                .map(OptionValue::getIdOption)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
            
            if (allOptionIds.isEmpty()) {
                return new HashMap<>();
            }
            
            // Batch fetch all options
            List<Option> allOptions = optionRepository.findAllById(allOptionIds);
            
            // Create map for quick lookup
            Map<Long, Option> optionMap = allOptions.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Option::getIdOption, o -> o, (existing, replacement) -> existing));
            
            // Group associations by variant ID and build VariantOptionDTO lists
            return allAssociations.stream()
                .filter(Objects::nonNull)
                .filter(association -> association.getIdVariant() != null && association.getIdOv() != null)
                .collect(Collectors.groupingBy(
                    VariantOptionValue::getIdVariant,
                    Collectors.mapping(
                        association -> {
                            OptionValue optionValue = optionValueMap.get(association.getIdOv());
                            if (optionValue == null) {
                                return null;
                            }
                            
                            Option option = optionMap.get(optionValue.getIdOption());
                            if (option == null) {
                                return null;
                            }
                            
                            return new VariantOptionDTO(
                                option.getIdOption(),
                                option.getLabel(),
                                optionValue.getIdOv(),
                                optionValue.getValue()
                            );
                        },
                        Collectors.filtering(Objects::nonNull, Collectors.toList())
                    )
                ));
                
        } catch (Exception e) {
            // Log error and return empty map to handle gracefully
            System.err.println("Error batch fetching variant options: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Creates a VariantWithOptionsDTO with basic variant information only.
     * Useful when option details are not needed or available.
     * 
     * @param variant the variant entity
     * @return VariantWithOptionsDTO with basic information, or null if variant is null
     * 
     * Requirement 3.3: Handle null values and missing associations gracefully
     */
    public VariantWithOptionsDTO mapToDTOWithoutOptions(Variant variant) {
        if (variant == null) {
            return null;
        }
        
        VariantWithOptionsDTO dto = new VariantWithOptionsDTO();
        dto.setIdVariant(variant.getIdVariant());
        dto.setTitle(variant.getTitle());
        dto.setPrice(variant.getPrice());
        dto.setIdProduct(variant.getIdProduct());
        dto.setCreatedAt(variant.getCreatedAt());
        dto.setUpdatedAt(variant.getUpdatedAt());
        dto.setOptions(new ArrayList<>()); // Empty list instead of null
        
        return dto;
    }
    
    /**
     * Creates a VariantWithOptionsDTO with basic variant and stock information only.
     * Useful when option details are not needed or available.
     * 
     * @param variantInStock the variant in stock entity
     * @return VariantWithOptionsDTO with basic and stock information, or null if variantInStock is null
     * 
     * Requirement 3.3: Handle null values and missing associations gracefully
     */
    public VariantWithOptionsDTO mapToDTOWithoutOptions(VariantInStock variantInStock) {
        if (variantInStock == null) {
            return null;
        }
        
        VariantWithOptionsDTO dto = new VariantWithOptionsDTO();
        dto.setIdVariant(variantInStock.getIdVariant());
        dto.setTitle(variantInStock.getTitle());
        dto.setPrice(variantInStock.getPrice());
        dto.setIdProduct(variantInStock.getIdProduct());
        dto.setCreatedAt(variantInStock.getCreatedAt());
        dto.setUpdatedAt(variantInStock.getUpdatedAt());
        
        // Set stock information
        dto.setStockQuantity(variantInStock.getVariantNumber() != null ? 
            variantInStock.getVariantNumber().intValue() : null);
        dto.setStockStatus(variantInStock.getStockStatus());
        
        dto.setOptions(new ArrayList<>()); // Empty list instead of null
        
        return dto;
    }
}