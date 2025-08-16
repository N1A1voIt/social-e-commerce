package com.itu.socialcom.demo.products.service;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.dto.*;
import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.OptionValue;
import com.itu.socialcom.demo.products.repository.OptionRepository;
import com.itu.socialcom.demo.products.repository.OptionValueRepository;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.repository.VariantOptionValueRepository;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.model.VariantInStock;
import com.itu.socialcom.demo.products.variants.model.VariantOptionValue;
import com.itu.socialcom.demo.products.variants.repository.VariantInStockRepository;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of VariantService interface providing comprehensive variant management functionality
 * Handles variant creation, automatic generation, updates, deletion, and retrieval with option details
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VariantServiceImpl implements VariantService {
    
    private final VariantRepository variantRepository;
    private final VariantOptionValueRepository variantOptionValueRepository;
    private final OptionRepository optionRepository;
    private final OptionValueRepository optionValueRepository;
    private final ProductRepository productRepository;
    private final VariantInStockRepository variantInStockRepository;
    private final TokenV2ServiceImpl tokenService;
    
    @Override
    @Transactional
    public VariantWithOptionsDTO createVariantWithOptions(Long productId, CreateVariantWithOptionsRequest request, Long sellerId) {
        // Validate seller ownership
        validateSellerOwnership(productId, sellerId.intValue());
        
        // Validate option values belong to product and check for duplicates
        validateOptionValues(productId, request.getOptionValueIds());
        checkForDuplicateVariant(productId, request.getOptionValueIds());
        
        // Create variant
        Variant variant = new Variant(request.getTitle(), request.getPrice(), productId);
        variant = variantRepository.save(variant);
        
        // Create option value associations
        for (Long optionValueId : request.getOptionValueIds()) {
            VariantOptionValue association = new VariantOptionValue(optionValueId, variant.getIdVariant());
            variantOptionValueRepository.save(association);
        }
        
        // Return variant with option details
        return getVariantWithOptions(productId, variant.getIdVariant(), sellerId);
    }
    
    @Override
    public List<VariantWithOptionsDTO> generateAllVariantCombinations(Long productId, GenerateVariantsRequest request, Long sellerId) {
        // Validate seller ownership
        validateSellerOwnership(productId, sellerId.intValue());
        
        // Fetch product options
        List<Option> options = optionRepository.findByIdProduct(productId);
        if (options.isEmpty()) {
            throw new IllegalStateException("No options exist for this product. Cannot generate variants.");
        }
        
        // Fetch option values for all options
        List<Long> optionIds = options.stream().map(Option::getIdOption).collect(Collectors.toList());
        List<OptionValue> allOptionValues = optionValueRepository.findByIdOptionIn(optionIds);
        
        // Group option values by option
        Map<Long, List<OptionValue>> optionValuesMap = allOptionValues.stream()
                .collect(Collectors.groupingBy(OptionValue::getIdOption));
        
        // Generate all combinations
        List<List<OptionValue>> combinations = generateCartesianProduct(options, optionValuesMap);
        
        List<VariantWithOptionsDTO> createdVariants = new ArrayList<>();
        
        for (List<OptionValue> combination : combinations) {
            List<Long> optionValueIds = combination.stream()
                    .map(OptionValue::getIdOv)
                    .collect(Collectors.toList());
            
            // Check for duplicates if not overwriting
            if (!request.isOverwriteExisting() && isDuplicateVariant(productId, optionValueIds)) {
                continue; // Skip this combination
            }
            
            // Generate descriptive title
            String title = generateVariantTitle(request.getTitlePrefix(), combination);
            
            // Create variant
            Variant variant = new Variant(title, request.getBasePrice(), productId);
            variant = variantRepository.save(variant);
            
            // Create option value associations
            for (Long optionValueId : optionValueIds) {
                VariantOptionValue association = new VariantOptionValue(optionValueId, variant.getIdVariant());
                variantOptionValueRepository.save(association);
            }
            
            // Add to results
            VariantWithOptionsDTO variantDTO = buildVariantWithOptionsDTO(variant, combination);
            createdVariants.add(variantDTO);
        }
        
        return createdVariants;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VariantWithOptionsDTO> getProductVariantsWithOptions(Long productId, Long sellerId) {
        // Validate seller ownership
        validateSellerOwnership(productId, sellerId.intValue());
        
        // Fetch variants with stock information
        List<VariantInStock> variantsInStock = variantInStockRepository.findVariantInStockByIdProduct(productId);
        
        List<VariantWithOptionsDTO> result = new ArrayList<>();
        
        for (VariantInStock variantInStock : variantsInStock) {
            // Fetch option value associations
            List<VariantOptionValue> associations = variantOptionValueRepository.findByIdVariant(variantInStock.getIdVariant());
            
            // Fetch option details
            List<VariantOptionDTO> optionDTOs = buildVariantOptionDTOs(associations);
            
            // Build DTO
            VariantWithOptionsDTO dto = new VariantWithOptionsDTO();
            dto.setIdVariant(variantInStock.getIdVariant());
            dto.setTitle(variantInStock.getTitle());
            dto.setPrice(variantInStock.getPrice());
            dto.setIdProduct(variantInStock.getIdProduct());
            dto.setCreatedAt(variantInStock.getCreatedAt());
            dto.setUpdatedAt(variantInStock.getUpdatedAt());
            dto.setStockQuantity(variantInStock.getVariantNumber() != null ? variantInStock.getVariantNumber().intValue() : 0);
            dto.setStockStatus(variantInStock.getStockStatus());
            dto.setOptions(optionDTOs);
            
            result.add(dto);
        }
        
        // Sort by creation date descending
        result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        return result;
    }
    
    @Override
    public VariantWithOptionsDTO updateVariant(Long productId, Long variantId, UpdateVariantRequest request, Long sellerId) {
        // Validate seller ownership and variant existence
        validateSellerOwnership(productId, sellerId.intValue());
        
        Variant variant = variantRepository.findByIdVariantAndIdProduct(variantId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found or does not belong to this product"));
        
        // Update only provided fields
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            variant.setTitle(request.getTitle());
        }
        
        if (request.getPrice() != null) {
            variant.setPrice(request.getPrice());
        }
        
        // Save variant (timestamp will be updated by @PreUpdate)
        variant = variantRepository.save(variant);
        
        // Return updated variant with option details
        return getVariantWithOptions(productId, variantId, sellerId);
    }
    
    @Override
    public void deleteVariant(Long productId, Long variantId, Long sellerId) {
        // Validate seller ownership
        validateSellerOwnership(productId, sellerId.intValue());
        
        // Verify variant exists and belongs to product
        if (!variantRepository.existsByIdVariantAndIdProduct(variantId, productId)) {
            throw new IllegalArgumentException("Variant not found or does not belong to this product");
        }
        
        // Delete option value associations first (cascade deletion)
        variantOptionValueRepository.deleteByIdVariant(variantId);
        
        // Delete variant
        variantRepository.deleteById(variantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public VariantWithOptionsDTO getVariantWithOptions(Long productId, Long variantId, Long sellerId) {
        // Validate seller ownership
        validateSellerOwnership(productId, sellerId.intValue());
        
        // Fetch variant
        Variant variant = variantRepository.findByIdVariantAndIdProduct(variantId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found or does not belong to this product"));
        
        // Fetch option value associations
        List<VariantOptionValue> associations = variantOptionValueRepository.findByIdVariant(variantId);
        
        // Fetch option details
        List<VariantOptionDTO> optionDTOs = buildVariantOptionDTOs(associations);
        
        // Build and return DTO
        VariantWithOptionsDTO dto = new VariantWithOptionsDTO();
        dto.setIdVariant(variant.getIdVariant());
        dto.setTitle(variant.getTitle());
        dto.setPrice(variant.getPrice());
        dto.setIdProduct(variant.getIdProduct());
        dto.setCreatedAt(variant.getCreatedAt());
        dto.setUpdatedAt(variant.getUpdatedAt());
        dto.setOptions(optionDTOs);
        
        // Try to get stock information
        List<VariantInStock> stockInfo = variantInStockRepository.findVariantInStockByIdProduct(productId);
        stockInfo.stream()
                .filter(stock -> stock.getIdVariant().equals(variantId))
                .findFirst()
                .ifPresent(stock -> {
                    dto.setStockQuantity(stock.getVariantNumber() != null ? stock.getVariantNumber().intValue() : 0);
                    dto.setStockStatus(stock.getStockStatus());
                });
        
        return dto;
    }
    
    // Helper methods
    
    private void validateSellerOwnership(Long productId, Integer sellerId) {
        if (!productRepository.existsByIdProductAndIdSeller(productId, sellerId)) {
            throw new IllegalArgumentException("Product not found or you don't have permission to access it");
        }
    }
    
    private void validateOptionValues(Long productId, List<Long> optionValueIds) {
        // Fetch all option values
        List<OptionValue> optionValues = optionValueRepository.findAllById(optionValueIds);
        
        if (optionValues.size() != optionValueIds.size()) {
            throw new IllegalStateException("One or more option values not found");
        }
        
        // Get all option IDs from the option values
        List<Long> optionIds = optionValues.stream()
                .map(OptionValue::getIdOption)
                .distinct()
                .collect(Collectors.toList());
        
        // Fetch options to verify they belong to the product
        List<Option> options = optionRepository.findAllById(optionIds);
        
        boolean allBelongToProduct = options.stream()
                .allMatch(option -> option.getIdProduct().equals(productId));
        
        if (!allBelongToProduct) {
            throw new IllegalStateException("One or more option values do not belong to this product");
        }
        
        // Verify each option has exactly one value selected
        Map<Long, Long> optionValueCount = optionValues.stream()
                .collect(Collectors.groupingBy(OptionValue::getIdOption, Collectors.counting()));
        
        List<Option> productOptions = optionRepository.findByIdProduct(productId);
        for (Option option : productOptions) {
            Long count = optionValueCount.get(option.getIdOption());
            if (count == null || count == 0) {
                throw new IllegalStateException("Option '" + option.getLabel() + "' must have exactly one value selected");
            }
            if (count > 1) {
                throw new IllegalStateException("Option '" + option.getLabel() + "' can only have one value selected");
            }
        }
    }
    
    private void checkForDuplicateVariant(Long productId, List<Long> optionValueIds) {
        if (isDuplicateVariant(productId, optionValueIds)) {
            throw new IllegalStateException("A variant with this option value combination already exists");
        }
    }
    
    private boolean isDuplicateVariant(Long productId, List<Long> optionValueIds) {
        // Get all variants for the product
        List<Variant> existingVariants = variantRepository.findByIdProduct(productId);
        
        for (Variant variant : existingVariants) {
            List<VariantOptionValue> existingAssociations = variantOptionValueRepository.findByIdVariant(variant.getIdVariant());
            List<Long> existingOptionValueIds = existingAssociations.stream()
                    .map(VariantOptionValue::getIdOv)
                    .sorted()
                    .collect(Collectors.toList());
            
            List<Long> sortedNewIds = optionValueIds.stream().sorted().collect(Collectors.toList());
            
            if (existingOptionValueIds.equals(sortedNewIds)) {
                return true;
            }
        }
        
        return false;
    }
    
    private List<List<OptionValue>> generateCartesianProduct(List<Option> options, Map<Long, List<OptionValue>> optionValuesMap) {
        List<List<OptionValue>> result = new ArrayList<>();
        generateCombinationsRecursive(options, optionValuesMap, 0, new ArrayList<>(), result);
        return result;
    }
    
    private void generateCombinationsRecursive(List<Option> options, Map<Long, List<OptionValue>> optionValuesMap, 
                                             int optionIndex, List<OptionValue> currentCombination, 
                                             List<List<OptionValue>> result) {
        if (optionIndex == options.size()) {
            result.add(new ArrayList<>(currentCombination));
            return;
        }
        
        Option currentOption = options.get(optionIndex);
        List<OptionValue> values = optionValuesMap.get(currentOption.getIdOption());
        
        if (values != null) {
            for (OptionValue value : values) {
                currentCombination.add(value);
                generateCombinationsRecursive(options, optionValuesMap, optionIndex + 1, currentCombination, result);
                currentCombination.remove(currentCombination.size() - 1);
            }
        }
    }
    
    private String generateVariantTitle(String prefix, List<OptionValue> combination) {
        String optionValues = combination.stream()
                .map(OptionValue::getValue)
                .collect(Collectors.joining(" / "));
        return prefix + " - " + optionValues;
    }
    
    private VariantWithOptionsDTO buildVariantWithOptionsDTO(Variant variant, List<OptionValue> optionValues) {
        List<VariantOptionDTO> optionDTOs = new ArrayList<>();
        
        // Get option details for each option value
        for (OptionValue optionValue : optionValues) {
            Option option = optionRepository.findById(optionValue.getIdOption()).orElse(null);
            if (option != null) {
                VariantOptionDTO optionDTO = new VariantOptionDTO(
                    option.getIdOption(),
                    option.getLabel(),
                    optionValue.getIdOv(),
                    optionValue.getValue()
                );
                optionDTOs.add(optionDTO);
            }
        }
        
        return new VariantWithOptionsDTO(
            variant.getIdVariant(),
            variant.getTitle(),
            variant.getPrice(),
            variant.getIdProduct(),
            variant.getCreatedAt(),
            variant.getUpdatedAt(),
            null, // Stock quantity will be set if available
            null, // Stock status will be set if available
            optionDTOs
        );
    }
    
    private List<VariantOptionDTO> buildVariantOptionDTOs(List<VariantOptionValue> associations) {
        List<VariantOptionDTO> optionDTOs = new ArrayList<>();
        
        for (VariantOptionValue association : associations) {
            // Fetch option value
            OptionValue optionValue = optionValueRepository.findById(association.getIdOv()).orElse(null);
            if (optionValue != null) {
                // Fetch option
                Option option = optionRepository.findById(optionValue.getIdOption()).orElse(null);
                if (option != null) {
                    VariantOptionDTO optionDTO = new VariantOptionDTO(
                        option.getIdOption(),
                        option.getLabel(),
                        optionValue.getIdOv(),
                        optionValue.getValue()
                    );
                    optionDTOs.add(optionDTO);
                }
            }
        }
        
        return optionDTOs;
    }
}