package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.messages.MessageService;
import com.itu.socialcom.demo.messages.MessagingFactory;
import com.itu.socialcom.demo.messages.messagechild.MessageChild;
import com.itu.socialcom.demo.messages.messagechild.MessageChildRepository;
import com.itu.socialcom.demo.messages.messagemother.MessageMother;
import com.itu.socialcom.demo.messages.messagemother.MessageMotherRepository;
import com.itu.socialcom.demo.orders.DownPayment;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.repository.DownPaymentRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.orders.tempLink.TempLink;
import com.itu.socialcom.demo.orders.tempLink.TempLinkService;
import com.itu.socialcom.demo.potentialCustomers.entity.PotentialCustomerV2;
import com.itu.socialcom.demo.potentialCustomers.repository.PotentialCustomerV2Service;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderPaymentLink implements OrderPaymentLinkService{
    @Autowired
    private TempLinkService tempLinkService;
    @Autowired
    private OrderCreationService orderCreationService;
    @Autowired
    private MessagingFactory messageService;
    @Autowired
    PotentialCustomerV2Service potentialCustomerV2Service;
    @Autowired
    ManagedPageCPLRepository managedPageCPLRepository;
    @Autowired
    OrderParentRepository orderParentRepository;
    @Autowired
    DownPaymentRepository downPaymentRepository;
    @Autowired
    MessageChildRepository messageChildRepository;
    @Autowired
    MessageMotherRepository messageMotherRepository;
    @Override
    @Transactional
    public OrderParent askUserToPay(OrderParent orderParent) throws Exception {
        if (orderParent == null) {
            throw new IllegalArgumentException("Order parent or its children cannot be null or empty");
        }
        PotentialCustomerV2 potentialCustomerV2 = potentialCustomerV2Service.findById(orderParent.getIdPc()).orElse(null);
        if (potentialCustomerV2 == null) {
            throw new IllegalArgumentException("Potential customer not found for the given ID");
        }
        DownPayment downPayment = this.downPaymentRepository.findByIdSeller(orderParent.getIdSeller().longValue()).get(0);
        double downPaymentAmount = downPayment.getPaymentInPercent()/100 * orderParent.getDTotal();
        TempLink tempLink = tempLinkService.createLink(orderParent.getCustomerNumber(), orderParent.getIdOrderM().intValue(),
                orderParent.getIdSeller(),downPaymentAmount);

        String message = "Please pay for your order using the following link: " +
                tempLink.getTempLink() + " just know that this link will expire in 1 hour and you won't be able to claim a new link , the down payment amount is " + downPaymentAmount +" Ariary ";

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
        orderParent.setDStatus(5);
        orderParentRepository.save(orderParent);
        return orderParent;
    }
}
