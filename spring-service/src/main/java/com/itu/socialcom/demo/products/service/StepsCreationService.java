package com.itu.socialcom.demo.products.service;

import com.itu.socialcom.demo.products.dto.CreationStepsDTO;

public interface StepsCreationService {
    CreationStepsDTO saveStep1(CreationStepsDTO creationStepsDTO,String token) throws Exception;
    CreationStepsDTO saveStep2(CreationStepsDTO creationStepsDTO,String token) throws Exception;
}
