package com.itu.socialcom.demo.messages.fetchskus;

import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class FetchSkusQtyFromPython {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${python.service.url}")
    private String pythonServiceUrl;
    @Autowired
    private VariantRepository variantRepository;

    public VariantSkuResponse getVariantsFromPythonService(String inputText) {
        String url = pythonServiceUrl + "/extract-skus-qty";
        ResponseEntity<VariantSkuResponse> response = restTemplate.postForEntity(
                url, inputText, VariantSkuResponse.class);
        return response.getBody();
    }
    public List<Variant> fetchVariants(UserQuery userQuery,Long userId) {
        VariantSkuResponse variantSkuResponse = getVariantsFromPythonService(userQuery.getQuery());
        if (variantSkuResponse != null && variantSkuResponse.getVariants() != null) {
            List<String> skus = variantSkuResponse.getVariants().stream()
                    .map(VariantSkuQty::getSku)
                    .toList();
            return variantRepository.findByIdSellerAndSkuIn(userId, skus);
        }
        return List.of();
    }
}
