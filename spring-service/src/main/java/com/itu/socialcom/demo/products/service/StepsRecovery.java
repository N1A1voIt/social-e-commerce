package com.itu.socialcom.demo.products.service;

import com.itu.socialcom.demo.products.dto.CreationStepsDTO;
import com.itu.socialcom.demo.products.dto.DisplayContainer;

public interface StepsRecovery {
    DisplayContainer recoverStep1(String token) throws Exception;
}
