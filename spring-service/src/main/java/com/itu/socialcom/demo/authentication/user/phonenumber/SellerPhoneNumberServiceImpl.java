package com.itu.socialcom.demo.authentication.user.phonenumber;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.authentication.user.SellerRepository;
import com.itu.socialcom.demo.authentication.user.phonenumber.dto.SellerPhoneNumberRequest;
import com.itu.socialcom.demo.authentication.user.phonenumber.dto.SellerPhoneNumberResponse;
import com.itu.socialcom.demo.moneytransactions.PaymentMethod;
import com.itu.socialcom.demo.moneytransactions.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellerPhoneNumberServiceImpl implements SellerPhoneNumberService {

    @Autowired
    private SellerPhoneNumberRepository sellerPhoneNumberRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Override
    @Transactional
    public SellerPhoneNumberResponse createOrUpdate(Long sellerId, SellerPhoneNumberRequest request) {
        // Validate seller exists
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found with id: " + sellerId));

        // Validate payment method exists
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException("Payment method not found with id: " + request.getPaymentMethodId()));

        // Check if configuration already exists for this seller and payment method
        SellerPhoneNumber sellerPhoneNumber = sellerPhoneNumberRepository
                .findBySellerIdAndPaymentMethodId(sellerId, request.getPaymentMethodId())
                .orElse(new SellerPhoneNumber());

        // Update fields
        sellerPhoneNumber.setPhoneNumber(request.getPhoneNumber());
        sellerPhoneNumber.setAssociatedName(request.getAssociatedName());
        sellerPhoneNumber.setSeller(seller);
        sellerPhoneNumber.setPaymentMethod(paymentMethod);

        // Save
        sellerPhoneNumber = sellerPhoneNumberRepository.save(sellerPhoneNumber);

        return SellerPhoneNumberResponse.fromEntity(sellerPhoneNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerPhoneNumberResponse> getAllBySellerId(Long sellerId) {
        // Validate seller exists
        if (!sellerRepository.existsById(sellerId)) {
            throw new RuntimeException("Seller not found with id: " + sellerId);
        }

        List<SellerPhoneNumber> phoneNumbers = sellerPhoneNumberRepository.findBySellerIdOrderByIdAsc(sellerId);
        return phoneNumbers.stream()
                .map(SellerPhoneNumberResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SellerPhoneNumberResponse getById(Long id) {
        SellerPhoneNumber sellerPhoneNumber = sellerPhoneNumberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phone number configuration not found with id: " + id));
        return SellerPhoneNumberResponse.fromEntity(sellerPhoneNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerPhoneNumberResponse getBySellerAndPaymentMethod(Long sellerId, Long paymentMethodId) {
        SellerPhoneNumber sellerPhoneNumber = sellerPhoneNumberRepository
                .findBySellerIdAndPaymentMethodId(sellerId, paymentMethodId)
                .orElseThrow(() -> new RuntimeException(
                        "Phone number configuration not found for seller " + sellerId +
                        " and payment method " + paymentMethodId));
        return SellerPhoneNumberResponse.fromEntity(sellerPhoneNumber);
    }

    @Override
    @Transactional
    public void delete(Long id, Long sellerId) {
        SellerPhoneNumber sellerPhoneNumber = sellerPhoneNumberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phone number configuration not found with id: " + id));

        // Verify the phone number belongs to the seller
        if (!sellerPhoneNumber.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized: Phone number configuration does not belong to this seller");
        }

        sellerPhoneNumberRepository.delete(sellerPhoneNumber);
    }
}

