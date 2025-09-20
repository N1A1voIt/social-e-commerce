package com.itu.socialcom.demo.authentication.user.phonenumber;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerPhoneNumberRepository extends JpaRepository<SellerPhoneNumber, Long> {
//    SellerPhoneNumber findByPhoneNumber(String phoneNumber);
}
