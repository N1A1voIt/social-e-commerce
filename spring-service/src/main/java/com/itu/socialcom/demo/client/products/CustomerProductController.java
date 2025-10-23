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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            Customer customer = customerRepository.findCustomerByToken(token).orElse(null);
            if (customer == null) throw new Exception("Not logged in");
            return ResponseEntity.ok(productCplRepository.findByProductNumberGreaterThan(0.0,pageable));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/{productId}/options")
    public ResponseEntity<?> getProductOptions(@PathVariable Long productId, @RequestHeader("Authorization") String token) {
        try {
            // Verify customer is logged in
            Customer customer = customerRepository.findCustomerByToken(token).orElse(null);
            if (customer == null) throw new Exception("Not logged in");

            // Get all options for the product
            List<Option> options = optionRepository.findByIdProduct(productId);

            // Convert to DTOs
            List<ProductOptionDTO> optionDTOs = new ArrayList<>();
            for (Option option : options) {
                ProductOptionDTO optionDTO = new ProductOptionDTO();
                optionDTO.setIdOption(option.getIdOption());
                optionDTO.setLabel(option.getLabel());

                // Get option values for this option
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
            Customer customer = customerRepository.findCustomerByToken(token).orElse(null);
            if (customer == null) throw new Exception("Not logged in");

            Long productId = request.getProductId();
            List<Long> selectedOptionValueIds = request.getOptionValueIds();

            if (productId == null || selectedOptionValueIds == null || selectedOptionValueIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Product ID and at least one option value ID are required"));
            }

            // Get all variants for the product
            List<VariantInStock> variants = variantInStockRepository.findVariantInStockByIdProduct(productId);

            // Find variants that have all the selected option values
            for (Long optionValueId : selectedOptionValueIds) {
                // Get all variants that have this option value
                List<Long> variantIdsWithOptionValue = variantOptionValueRepository.findByIdOv(optionValueId)
                    .stream()
                    .map(vov -> vov.getIdVariant())
                    .collect(Collectors.toList());

                // Filter variants to only include those that have this option value
                variants = variants.stream()
                    .filter(v -> variantIdsWithOptionValue.contains(v.getIdVariant()))
                    .collect(Collectors.toList());

                if (variants.isEmpty()) {
                    break; // No variants match all selected option values
                }
            }

            if (variants.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No variant found with the selected option values"));
            }

            // Return the first matching variant (there should be only one if option values are properly set up)
            return ResponseEntity.ok(variants.get(0));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
