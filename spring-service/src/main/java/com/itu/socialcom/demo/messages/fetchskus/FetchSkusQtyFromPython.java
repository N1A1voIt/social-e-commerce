package com.itu.socialcom.demo.messages.fetchskus;

import com.itu.socialcom.demo.messages.dtol.VariantWithQuantity;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.relational.core.sql.In;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class FetchSkusQtyFromPython {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${python.service.url}")
    private String pythonServiceUrl;
    @Autowired
    private VariantRepository variantRepository;

    public VariantSkuResponse getVariantsFromPythonService(UserQuery userQuery, String token) {
        String url = pythonServiceUrl + "/extract-skus-qty";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);
        HttpEntity<UserQuery> requestEntity = new HttpEntity<>(userQuery, headers);
        ResponseEntity<VariantSkuResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                VariantSkuResponse.class
        );
        return response.getBody();
    }

    public List<VariantWithQuantity> fetchVariants(UserQuery userQuery, Long userId,String token) {
        VariantSkuResponse variantSkuResponse = getVariantsFromPythonService(userQuery,token);
        if (variantSkuResponse != null && variantSkuResponse.getVariants() != null) {
            HashMap<String,Integer> skuQtyMap = new HashMap<>();
            for (VariantSkuQty variantSkuQty : variantSkuResponse.getVariants()) {
                skuQtyMap.put(variantSkuQty.getSku(), variantSkuQty.getQty());
            }
            List<String> skus = List.copyOf(skuQtyMap.keySet());
            List<Variant> variants = variantRepository.findByIdSellerAndSkuIn(userId, skus);
            List<VariantWithQuantity> variantWithQuantities = new ArrayList<>();
            for (Variant variant : variants) {
                VariantWithQuantity variantWithQuantity = new VariantWithQuantity();
                variantWithQuantity.setVariant(variant);
                variantWithQuantity.setQuantity(skuQtyMap.get(variant.getSku()));
                variantWithQuantities.add(variantWithQuantity);
            }
            return variantWithQuantities;
        }
        return List.of();
    }
}
