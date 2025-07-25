package com.itu.socialcom.demo.products.service;

import com.itu.socialcom.demo.products.dto.CreationStepsDTO;

public interface StepsRecovery {
    CreationStepsDTO recoverStep1(String token) throws Exception;
}
