package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.authentication.user.phonenumber.SellerPhoneNumber;
import com.itu.socialcom.demo.messages.MessageService;
import com.itu.socialcom.demo.messages.MessagingFactory;
import com.itu.socialcom.demo.messages.messagechild.MessageChild;
import com.itu.socialcom.demo.messages.messagechild.MessageChildRepository;
import com.itu.socialcom.demo.messages.messagemother.MessageMother;
import com.itu.socialcom.demo.messages.messagemother.MessageMotherRepository;
import com.itu.socialcom.demo.moneytransactions.PaymentRequest;
import com.itu.socialcom.demo.moneytransactions.PaymentResponse;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.dto.PaymentDTO;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.orders.tempLink.TempLink;
import com.itu.socialcom.demo.potentialCustomers.entity.PotentialCustomerV2;
import com.itu.socialcom.demo.potentialCustomers.repository.PotentialCustomerV2Repository;
import com.itu.socialcom.demo.sales.Payments;
import com.itu.socialcom.demo.sales.Sales;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPagesNumber;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPagesNumberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderDeliveredService {
    @Autowired
    private OrderParentRepository orderParentRepository;
    @Autowired
    private MessageMotherRepository messageMotherRepository;
    @Autowired
    private PotentialCustomerV2Repository potentialCustomerV2Repository;
    @Autowired
    private MessageChildRepository messageChildRepository;
    @Autowired
    MessagingFactory messageService;
    @Autowired
    ManagedPageCPLRepository managedPageCPLRepository;
    @Transactional
    public void notifyCustomer(Long orderId) throws Exception {
        try {
            OrderParent orderParent = orderParentRepository.findByIdOrderM(orderId).get(0);
            if (orderParent == null) throw new Exception("Order not found");
            if (orderParent.getIdManagedPages() == null) throw new Exception("Order Is not manage by a page");
            PotentialCustomerV2 potentialCustomerV2 = potentialCustomerV2Repository.findById(orderParent.getIdPc()).orElse(null);
            String message = "Your order has been delivered. Now please tell us , which payment method you prefer to make the payment.(Mvola or cash)";
            if (orderParent.getIdManagedPages() == null) {

            } else {
                MessageMother messageMother = messageMotherRepository.findByIdPcAndIdMp(potentialCustomerV2.getId(), orderParent.getIdManagedPages());
                MessageChild messageChild = new MessageChild();
                messageChild.setIdMm(messageMother.getId());
                messageChild.setFromPlatform(false);
                messageChild.setMessage(message);
                messageChild.setCreatedAt(LocalDateTime.now());
                this.messageChildRepository.save(messageChild);
                MessageService messageService1 = messageService.getMessageService(potentialCustomerV2.getPlatform());
                ManagedPageCPL managedPageCPL = managedPageCPLRepository.findByIdMp(Long.valueOf(orderParent.getIdManagedPages()));
                messageService1.sendMessage(potentialCustomerV2.getIdentifierOnPlatform(),
                        message
                        ,managedPageCPL.getRefreshToken());
            }
            orderParent.setDStatus(41);
            orderParentRepository.save(orderParent);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Payment processing failed: " + e.getMessage());
        }
    }
}
