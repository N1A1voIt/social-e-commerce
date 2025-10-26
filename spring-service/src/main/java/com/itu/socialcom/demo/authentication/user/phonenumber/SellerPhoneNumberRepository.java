package com.itu.socialcom.demo.authentication.user.phonenumber;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerPhoneNumberRepository extends JpaRepository<SellerPhoneNumber, Long> {
    Optional<SellerPhoneNumber> findByIdSellerAndIdPm(Long idSeller, Long idPm);
//    SellerPhoneNumber findByPhoneNumber(String phoneNumber);
}
