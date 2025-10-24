package com.itu.socialcom.demo.client.products;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.client.customer.Customer;
import com.itu.socialcom.demo.client.customer.CustomerRepository;
import com.itu.socialcom.demo.client.customertoken.CustomerTokenServiceImpl;
import com.itu.socialcom.demo.client.products.dto.OptionValueDTO;
import com.itu.socialcom.demo.client.products.dto.ProductOptionDTO;
import com.itu.socialcom.demo.client.products.dto.SelectedOptionValuesRequest;
import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.OptionValue;
import com.itu.socialcom.demo.products.model.ProductCPL;
import com.itu.socialcom.demo.products.repository.OptionRepository;
import com.itu.socialcom.demo.products.repository.OptionValueRepository;
import com.itu.socialcom.demo.products.repository.ProductCplRepository;
import com.itu.socialcom.demo.products.repository.VariantOptionValueRepository;
import com.itu.socialcom.demo.products.variants.model.VariantInStock;
import com.itu.socialcom.demo.products.variants.repository.VariantInStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/customer/products")
public class CustomerProductController {
    @Autowired
    private ProductCplRepository productCplRepository;
    @Autowired
    private CustomerTokenServiceImpl customerRepository;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private OptionValueRepository optionValueRepository;
    @Autowired
    private VariantInStockRepository variantInStockRepository;
    @Autowired
    private VariantOptionValueRepository variantOptionValueRepository;

    @GetMapping
    public ResponseEntity<List<ProductCPL>> productsCpl(@RequestHeader("Authorization") String token, Pageable pageable) {
        try {
            System.out.println("Token received: " + token);
            Customer customer = customerRepository.findCustomerByToken(token.replace("Bearer ","")).orElse(null);
            if (customer == null) throw new Exception("Not logged in");
            return ResponseEntity.ok(productCplRepository.findByProductNumberGreaterThan(0.0,pageable));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/{productId}/options")
    public ResponseEntity<?> getProductOptions(@PathVariable Long productId, @RequestHeader("Authorization") String token) {
        try {
            Customer customer = customerRepository.findCustomerByToken(token.replace("Bearer ","")).orElse(null);
            if (customer == null) throw new Exception("Not logged in");

            List<Option> options = optionRepository.findByIdProduct(productId);

            List<ProductOptionDTO> optionDTOs = new ArrayList<>();
            for (Option option : options) {
                ProductOptionDTO optionDTO = new ProductOptionDTO();
                optionDTO.setIdOption(option.getIdOption());
                optionDTO.setLabel(option.getLabel());

                List<OptionValue> optionValues = optionValueRepository.findByIdOption(option.getIdOption());
                List<OptionValueDTO> optionValueDTOs = optionValues.stream()
                    .map(ov -> new OptionValueDTO(ov.getIdOv(), ov.getValue()))
                    .collect(Collectors.toList());

                optionDTO.setOptionValues(optionValueDTOs);
                optionDTOs.add(optionDTO);
            }

            return ResponseEntity.ok(optionDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/variants")
    public ResponseEntity<?> getVariantByOptionValues(@RequestBody SelectedOptionValuesRequest request,
                                                      @RequestHeader("Authorization") String token) {
        try {
            // Verify customer is logged in
            Customer customer = customerRepository.findCustomerByToken(token.replace("Bearer ", ""))
                    .orElseThrow(() -> new Exception("Not logged in"));

            Long productId = request.getProductId();
            List<Long> selectedOptionValueIds = request.getOptionValueIds();

            // Validate input
            if (productId == null || selectedOptionValueIds == null || selectedOptionValueIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Product ID and at least one option value ID are required"));
            }

            // Find variant matching all selected option values using a single optimized query
            Optional<VariantInStock> matchingVariant = findVariantWithAllOptionValues(
                    productId,
                    selectedOptionValueIds
            );

            return matchingVariant
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.ok(new VariantInStock()));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    private Optional<VariantInStock> findVariantWithAllOptionValues(Long productId, List<Long> optionValueIds) {
        int requiredMatches = optionValueIds.size();

        // Single query approach: Find variants that have ALL the specified option values
        // This uses a GROUP BY with HAVING COUNT to ensure all option values match
        List<Long> matchingVariantIds = variantOptionValueRepository
                .findVariantIdsWithAllOptionValues(optionValueIds, requiredMatches);

        if (matchingVariantIds.isEmpty()) {
            return Optional.empty();
        }

        // Get the full variant data for the matching variant
        return variantInStockRepository
                .findByIdProductAndIdVariantIn(productId, matchingVariantIds)
                .stream()
                .findFirst();
    }
}
