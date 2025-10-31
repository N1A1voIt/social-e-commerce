package com.itu.socialcom.demo.authentication.user.phonenumber;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerPhoneNumberRepository extends JpaRepository<SellerPhoneNumber, Long> {
    List<SellerPhoneNumber> findBySellerIdOrderByIdAsc(Long sellerId);
    Optional<SellerPhoneNumber> findBySellerIdAndPaymentMethodId(Long sellerId, Long paymentMethodId);
    boolean existsBySellerIdAndPaymentMethodId(Long sellerId, Long paymentMethodId);

    Optional<SellerPhoneNumber> findBySeller_IdAndPaymentMethod_Id(Long sellerId, Long paymentMethodId);
}
