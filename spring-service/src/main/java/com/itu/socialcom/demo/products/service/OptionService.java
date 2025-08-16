package com.itu.socialcom.demo.products.service;

import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.OptionValue;
import com.itu.socialcom.demo.products.repository.OptionRepository;
import com.itu.socialcom.demo.products.repository.OptionValueRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OptionService {
    @Autowired
    OptionRepository optionRepository;
    @Autowired
    OptionValueRepository optionValueRepository;

    public List<Option> fetchOptionsByProductId(Long productId) {
        List<Option> options = optionRepository.findByIdProduct(productId);
        List<Long> optionIds = options.stream()
                .map(Option::getIdOption)
                .toList();
        List<OptionValue> optionValues = optionValueRepository.findByIdOptionIn(optionIds);
        Map<Long, List<OptionValue>> valuesByOptionId = optionValues.stream()
                .collect(Collectors.groupingBy(OptionValue::getIdOption));
        for (Option option : options) {
            option.setOptionValues(valuesByOptionId.getOrDefault(option.getIdOption(), List.of()));
        }
        return options;
    }
}
