package com.itu.socialcom.demo.products.service;

import com.itu.socialcom.demo.authentication.token.TokenV2ServiceImpl;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.dto.CreationStepsDTO;
import com.itu.socialcom.demo.products.dto.OptionValueDTO;
import com.itu.socialcom.demo.products.model.Option;
import com.itu.socialcom.demo.products.model.OptionValue;
import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.model.TempProduct;
import com.itu.socialcom.demo.products.repository.OptionRepository;
import com.itu.socialcom.demo.products.repository.OptionValueRepository;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.repository.TempProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public  class StepsCreationServiceImpl implements StepsCreationService{
    @Autowired
    private TempProductRepository tempProductRepository;
    @Autowired
    private TokenV2ServiceImpl tokenService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private OptionValueRepository optionValueRepository;
    @Override
    public CreationStepsDTO saveStep1(CreationStepsDTO creationStepsDTO,String token) throws Exception {
        token = token.replace("Bearer ","");
        Seller seller = tokenService.findSellerByToken(token).orElse(null);
        if (seller == null) throw new Exception("Seller not found for the provided token");
        creationStepsDTO.getStep1().setIdSeller(seller.getId());
        tempProductRepository.save(creationStepsDTO.getStep1());
        return creationStepsDTO;
    }

    @Override
    @Transactional
    public CreationStepsDTO saveStep2(CreationStepsDTO creationStepsDTO,String token) throws Exception {
        token = token.replace("Bearer ","");
        Seller seller = tokenService.findSellerByToken(token.replace("Bearer ","")).orElse(null);
        Product product = tempProductToProduct(creationStepsDTO.getStep1());
        productRepository.save(product);
        extractOptions(creationStepsDTO.getStep2(),product.getIdProduct());
        creationStepsDTO.getStep1().setState(false);
        tempProductRepository.save(creationStepsDTO.getStep1());
        return creationStepsDTO;
    }
    private List<Option> extractOptions(List<OptionValueDTO> optionValueDTO,Long idProduct) {
        return optionValueDTO.stream()
                .map(option -> {
                    Option opt = new Option();
                    opt.setLabel(option.getOptionLabels());
                    opt.setIdProduct(idProduct);
                    optionRepository.save(opt);
                    for (String value : option.getValues()) {
                        OptionValue optionValue = new OptionValue();
                        optionValue.setIdOption(opt.getIdOption());
                        optionValue.setValue(value);
                        optionValueRepository.save(optionValue);
                    }
                    return opt;
                }).toList();
    }
    private Product tempProductToProduct(TempProduct tempProduct) {
        Product product = new Product();
        product.setName(tempProduct.getName());
        product.setDescription(tempProduct.getDescription());
        product.setMedia(tempProduct.getMedia());
        product.setPrice(tempProduct.getPrice());
        product.setIdCategory(tempProduct.getIdCategory());
        product.setIdSeller(tempProduct.getIdSeller());
        System.out.println(tempProduct.getSkuPrefix());
        product.setSkuPrefix(tempProduct.getSkuPrefix());
        return product;
    }
}
