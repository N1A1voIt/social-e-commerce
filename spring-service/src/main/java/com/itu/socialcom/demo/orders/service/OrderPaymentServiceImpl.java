package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.authentication.user.phonenumber.SellerPhoneNumber;
import com.itu.socialcom.demo.authentication.user.phonenumber.SellerPhoneNumberRepository;
import com.itu.socialcom.demo.moneytransactions.PaymentRequest;
import com.itu.socialcom.demo.moneytransactions.PaymentResponse;
import com.itu.socialcom.demo.moneytransactions.mvola.MVolaProvider;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.dto.PaymentDTO;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.orders.tempLink.TempLink;
import com.itu.socialcom.demo.orders.tempLink.TempLinkRepository;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPagesNumber;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPagesNumberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderPaymentServiceImpl implements OrderPaymentService{
    @Autowired
    private TempLinkRepository tempLinkRepository;
    @Autowired
    private OrderParentRepository parentRepository;
    @Autowired
    private ManagedPagesNumberRepository managedPagesNumberRepository;
    @Autowired
    private SellerPhoneNumberRepository sellerPhoneNumberRepository;
    @Autowired
    private MVolaProvider mVolaProvider;
    @Autowired
    private OrderParentRepository orderParentRepository;

    @Override
    @Transactional
    public PaymentResponse processOrderPayment(PaymentDTO paymentDTO, String detailsIdentifier) throws Exception {
        try {

            TempLink tempLink = tempLinkRepository.findById(detailsIdentifier)
                    .orElseThrow(() -> new Exception("Invalid payment link."));
            if (tempLink.getUsed()) throw new Exception("This payment link has already been used.");
            OrderParent orderParent = parentRepository.findById(tempLink.getIdOrderM().longValue())
                    .orElseThrow(() -> new Exception("Order not found."));
            ManagedPagesNumber managedPagesNumber = managedPagesNumberRepository.findByIdMp(orderParent.getIdManagedPages().longValue());
            SellerPhoneNumber sellerPhoneNumber = sellerPhoneNumberRepository.findById(managedPagesNumber.getIdSpn())
                    .orElseThrow(() -> new Exception("Seller phone number not found."));
            PaymentRequest paymentRequest = createPaymentRequest(paymentDTO,orderParent,sellerPhoneNumber);
            PaymentResponse paymentResponse = mVolaProvider.initiateTransaction(paymentRequest);
            orderParent.setDStatus(11); //Ordered
            tempLink.setUsed(true);
            orderParentRepository.save(orderParent);
            tempLinkRepository.save(tempLink);
            return paymentResponse;
        } catch (Exception e) {
            throw new Exception("Payment processing failed: " + e.getMessage());
        }
    }
    private PaymentRequest createPaymentRequest(PaymentDTO paymentDTO,OrderParent parent, SellerPhoneNumber sellerPhoneNumber) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(paymentDTO.getAmount());
        paymentRequest.setCurrency("Ar");
        paymentRequest.setDescription("P" + parent.getIdOrderM());
        paymentRequest.setPayer(sellerPhoneNumber.getPhoneNumber());
        paymentRequest.setPayee(sellerPhoneNumber.getPhoneNumber());
        paymentRequest.setCustomerMsisdn(paymentDTO.getPhoneNumber());
        return paymentRequest;
    }
}
