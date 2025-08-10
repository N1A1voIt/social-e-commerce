package com.itu.socialcom.demo.products.util;

import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.OptionValue;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for generating variant combinations from product options.
 * Implements Cartesian product algorithm for option combinations and generates descriptive titles.
 */
@Component
public class VariantCombinationGenerator {
    
    /**
     * Maximum number of combinations to generate to prevent memory issues
     */
    private static final int MAX_COMBINATIONS = 1000;
    
    /**
     * Represents a single variant combination with option values and generated title
     */
    public static class VariantCombination {
        private final List<Long> optionValueIds;
        private final String title;
        private final Map<String, String> optionValueMap;
        
        public VariantCombination(List<Long> optionValueIds, String title, Map<String, String> optionValueMap) {
            this.optionValueIds = new ArrayList<>(optionValueIds);
            this.title = title;
            this.optionValueMap = new HashMap<>(optionValueMap);
        }
        
        public List<Long> getOptionValueIds() {
            return new ArrayList<>(optionValueIds);
        }
        
        public String getTitle() {
            return title;
        }
        
        public Map<String, String> getOptionValueMap() {
            return new HashMap<>(optionValueMap);
        }
    }
    
    /**
     * Configuration for combination generation
     */
    public static class GenerationConfig {
        private String titlePrefix = "Variant";
        private String titleSeparator = " - ";
        private int maxCombinations = MAX_COMBINATIONS;
        private boolean includeOptionLabels = true;
        private Set<String> excludeOptionLabels = new HashSet<>();
        
        public GenerationConfig withTitlePrefix(String titlePrefix) {
            this.titlePrefix = titlePrefix;
            return this;
        }
        
        public GenerationConfig withTitleSeparator(String titleSeparator) {
            this.titleSeparator = titleSeparator;
            return this;
        }
        
        public GenerationConfig withMaxCombinations(int maxCombinations) {
            this.maxCombinations = Math.min(maxCombinations, MAX_COMBINATIONS);
            return this;
        }
        
        public GenerationConfig withIncludeOptionLabels(boolean includeOptionLabels) {
            this.includeOptionLabels = includeOptionLabels;
            return this;
        }
        
        public GenerationConfig withExcludeOptionLabels(Set<String> excludeOptionLabels) {
            this.excludeOptionLabels = new HashSet<>(excludeOptionLabels);
            return this;
        }
        
        // Getters
        public String getTitlePrefix() { return titlePrefix; }
        public String getTitleSeparator() { return titleSeparator; }
        public int getMaxCombinations() { return maxCombinations; }
        public boolean isIncludeOptionLabels() { return includeOptionLabels; }
        public Set<String> getExcludeOptionLabels() { return new HashSet<>(excludeOptionLabels); }
    }
    
    /**
     * Generate all possible variant combinations from product options and their values.
     * Uses Cartesian product algorithm to create all possible combinations.
     * 
     * @param options List of product options
     * @param optionValues List of all option values for the product
     * @return List of variant combinations with generated titles
     * @throws IllegalArgumentException if options or optionValues are null or empty
     */
    public List<VariantCombination> generateAllCombinations(List<Option> options, List<OptionValue> optionValues) {
        return generateAllCombinations(options, optionValues, new GenerationConfig());
    }
    
    /**
     * Generate all possible variant combinations with custom configuration.
     * 
     * @param options List of product options
     * @param optionValues List of all option values for the product
     * @param config Configuration for generation behavior
     * @return List of variant combinations with generated titles
     * @throws IllegalArgumentException if options or optionValues are null or empty
     */
    public List<VariantCombination> generateAllCombinations(List<Option> options, List<OptionValue> optionValues, GenerationConfig config) {
        validateInputs(options, optionValues);
        
        if (config == null) {
            config = new GenerationConfig();
        }
        
        // Group option values by option ID
        Map<Long, List<OptionValue>> optionValuesByOption = groupOptionValuesByOption(optionValues);
        
        // Filter options based on configuration
        List<Option> filteredOptions = filterOptions(options, config);
        
        // Validate that all options have values
        validateOptionsHaveValues(filteredOptions, optionValuesByOption);
        
        // Calculate total combinations and check limit
        long totalCombinations = calculateTotalCombinations(filteredOptions, optionValuesByOption);
        if (totalCombinations > config.getMaxCombinations()) {
            throw new IllegalArgumentException(
                String.format("Too many combinations (%d). Maximum allowed: %d", 
                    totalCombinations, config.getMaxCombinations())
            );
        }
        
        // Generate combinations using Cartesian product
        return generateCartesianProduct(filteredOptions, optionValuesByOption, config);
    }
    
    /**
     * Generate descriptive title from option value combination.
     * 
     * @param optionValueCombination Map of option label to option value
     * @param config Configuration for title generation
     * @return Generated descriptive title
     */
    public String generateTitle(Map<String, String> optionValueCombination, GenerationConfig config) {
        if (optionValueCombination == null || optionValueCombination.isEmpty()) {
            return config.getTitlePrefix();
        }
        
        StringBuilder titleBuilder = new StringBuilder(config.getTitlePrefix());
        
        // Sort option labels for consistent title generation
        List<String> sortedLabels = optionValueCombination.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
        
        for (String optionLabel : sortedLabels) {
            String optionValue = optionValueCombination.get(optionLabel);
            titleBuilder.append(config.getTitleSeparator());
            
            if (config.isIncludeOptionLabels()) {
                titleBuilder.append(optionLabel).append(": ").append(optionValue);
            } else {
                titleBuilder.append(optionValue);
            }
        }
        
        return titleBuilder.toString();
    }
    
    /**
     * Estimate the number of combinations that would be generated.
     * 
     * @param options List of product options
     * @param optionValues List of all option values for the product
     * @return Estimated number of combinations
     */
    public long estimateCombinationCount(List<Option> options, List<OptionValue> optionValues) {
        if (options == null || options.isEmpty() || optionValues == null || optionValues.isEmpty()) {
            return 0;
        }
        
        Map<Long, List<OptionValue>> optionValuesByOption = groupOptionValuesByOption(optionValues);
        return calculateTotalCombinations(options, optionValuesByOption);
    }
    
    /**
     * Check if combination generation is feasible within limits.
     * 
     * @param options List of product options
     * @param optionValues List of all option values for the product
     * @param maxCombinations Maximum allowed combinations
     * @return true if generation is feasible, false otherwise
     */
    public boolean isCombinationGenerationFeasible(List<Option> options, List<OptionValue> optionValues, int maxCombinations) {
        long estimatedCount = estimateCombinationCount(options, optionValues);
        return estimatedCount > 0 && estimatedCount <= maxCombinations;
    }
    
    // Private helper methods
    
    private void validateInputs(List<Option> options, List<OptionValue> optionValues) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options list cannot be null or empty");
        }
        if (optionValues == null || optionValues.isEmpty()) {
            throw new IllegalArgumentException("Option values list cannot be null or empty");
        }
    }
    
    private Map<Long, List<OptionValue>> groupOptionValuesByOption(List<OptionValue> optionValues) {
        return optionValues.stream()
            .collect(Collectors.groupingBy(OptionValue::getIdOption));
    }
    
    private List<Option> filterOptions(List<Option> options, GenerationConfig config) {
        if (config.getExcludeOptionLabels().isEmpty()) {
            return options;
        }
        
        return options.stream()
            .filter(option -> !config.getExcludeOptionLabels().contains(option.getLabel()))
            .collect(Collectors.toList());
    }
    
    private void validateOptionsHaveValues(List<Option> options, Map<Long, List<OptionValue>> optionValuesByOption) {
        for (Option option : options) {
            List<OptionValue> values = optionValuesByOption.get(option.getIdOption());
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException(
                    String.format("Option '%s' has no values defined", option.getLabel())
                );
            }
        }
    }
    
    private long calculateTotalCombinations(List<Option> options, Map<Long, List<OptionValue>> optionValuesByOption) {
        long total = 1;
        for (Option option : options) {
            List<OptionValue> values = optionValuesByOption.get(option.getIdOption());
            if (values != null && !values.isEmpty()) {
                total *= values.size();
                // Check for overflow
                if (total > MAX_COMBINATIONS) {
                    return MAX_COMBINATIONS + 1; // Return value > max to indicate overflow
                }
            }
        }
        return total;
    }
    
    private List<VariantCombination> generateCartesianProduct(
            List<Option> options, 
            Map<Long, List<OptionValue>> optionValuesByOption, 
            GenerationConfig config) {
        
        List<VariantCombination> combinations = new ArrayList<>();
        
        // Prepare option value lists in order
        List<List<OptionValue>> optionValueLists = options.stream()
            .map(option -> optionValuesByOption.get(option.getIdOption()))
            .collect(Collectors.toList());
        
        // Generate Cartesian product
        generateCartesianProductRecursive(
            optionValueLists, 
            0, 
            new ArrayList<>(), 
            new ArrayList<>(), 
            combinations, 
            options, 
            config
        );
        
        return combinations;
    }
    
    private void generateCartesianProductRecursive(
            List<List<OptionValue>> optionValueLists,
            int currentIndex,
            List<OptionValue> currentCombination,
            List<Long> currentIds,
            List<VariantCombination> results,
            List<Option> options,
            GenerationConfig config) {
        
        if (currentIndex == optionValueLists.size()) {
            // Create combination
            Map<String, String> optionValueMap = new HashMap<>();
            for (int i = 0; i < currentCombination.size(); i++) {
                OptionValue optionValue = currentCombination.get(i);
                Option option = options.get(i);
                optionValueMap.put(option.getLabel(), optionValue.getValue());
            }
            
            String title = generateTitle(optionValueMap, config);
            results.add(new VariantCombination(new ArrayList<>(currentIds), title, optionValueMap));
            return;
        }
        
        List<OptionValue> currentOptionValues = optionValueLists.get(currentIndex);
        for (OptionValue optionValue : currentOptionValues) {
            currentCombination.add(optionValue);
            currentIds.add(optionValue.getIdOv());
            
            generateCartesianProductRecursive(
                optionValueLists, 
                currentIndex + 1, 
                currentCombination, 
                currentIds, 
                results, 
                options, 
                config
            );
            
            currentCombination.remove(currentCombination.size() - 1);
            currentIds.remove(currentIds.size() - 1);
        }
    }
}