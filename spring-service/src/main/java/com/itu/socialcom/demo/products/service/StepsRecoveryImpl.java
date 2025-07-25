package com.itu.socialcom.demo.products.service;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.dto.CreationStepsDTO;
import com.itu.socialcom.demo.products.dto.DisplayContainer;
import com.itu.socialcom.demo.products.model.Category;
import com.itu.socialcom.demo.products.model.TempProduct;
import com.itu.socialcom.demo.products.repository.CategoryRepository;
import com.itu.socialcom.demo.products.repository.TempProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StepsRecoveryImpl implements StepsRecovery{
    @Autowired
    TokenV2ServiceImpl tokenService;
    @Autowired
    TempProductRepository tempProductRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Override
    public DisplayContainer recoverStep1(String token) throws Exception {
        token = token.replace("Bearer ","");
        Seller seller = tokenService.findSellerByToken(token).orElse(null);
        CreationStepsDTO creationStepsDTO = new CreationStepsDTO();
        if (seller == null) throw new Exception("Seller not found for the provided token");
        TempProduct tempProduct = tempProductRepository.findByIdSellerAndState(seller.getId(), false);
        if (tempProduct != null) {
            creationStepsDTO.setStep1(tempProduct);
        }
        List<Category> categories = categoryRepository.findAll();
        DisplayContainer displayContainer = new DisplayContainer();
        displayContainer.setCreationStepsDTO(creationStepsDTO);
        displayContainer.setCategories(categories);
        return displayContainer;
    }
}
