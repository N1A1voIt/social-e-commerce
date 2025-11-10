package com.itu.socialcom.demo.potentialCustomers.repository;

import com.itu.socialcom.demo.potentialCustomers.entity.PotentialCustomerV2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PotentialCustomerV2Repository extends JpaRepository<PotentialCustomerV2,String> {
    Optional<PotentialCustomerV2> findByIdentifierOnPlatform(String identifierOnPlatform);
    
    Optional<PotentialCustomerV2> findFirstByIdentifierOnPlatform(String identifierOnPlatform);

    List<PotentialCustomerV2> findByIdentifierOnPlatformAndSupportedPlatform(String identifierOnPlatform, Long supportedPlatform);
}
