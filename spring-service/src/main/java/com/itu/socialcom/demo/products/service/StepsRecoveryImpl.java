package com.itu.socialcom.demo.products.service;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.dto.CreationStepsDTO;
import com.itu.socialcom.demo.products.model.TempProduct;
import com.itu.socialcom.demo.products.repository.TempProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StepsRecoveryImpl implements StepsRecovery{
    @Autowired
    TokenV2ServiceImpl tokenService;
    @Autowired
    TempProductRepository tempProductRepository;
    @Override
    public CreationStepsDTO recoverStep1(String token) throws Exception {
        Seller seller = tokenService.findSellerByToken(token).orElse(null);
        CreationStepsDTO creationStepsDTO = new CreationStepsDTO();
        if (seller == null) throw new Exception("Seller not found for the provided token");
        TempProduct tempProduct = tempProductRepository.findByIdSellerAndState(seller.getId(), true);
        if (tempProduct != null) {
            creationStepsDTO.setStep1(tempProduct);
        }
        return creationStepsDTO;
    }
}
