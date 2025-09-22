package com.itu.socialcom.demo.orders.delivery;

import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriver;
import com.itu.socialcom.demo.delivery.deliverydriver.DeliveryDriverRepository;
import com.itu.socialcom.demo.delivery.entity.AmountDistance;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.repository.AmountDistanceRepository;
import com.itu.socialcom.demo.delivery.repository.DeliveryRepository;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.dto.CallForTendersRequest;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.shipping.entity.ShippingPoint;
import com.itu.socialcom.demo.shipping.service.ShippingPointService;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPageCPLRepository;
import com.itu.socialcom.demo.utils.ApiResponse;
import com.itu.socialcom.demo.whatsapp.service.WhatsAppService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.swing.text.DateFormatter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CallForTenderServiceImpl implements CallForTenderService{
    @Autowired
    DeliveryRepository deliveryRepository;
    @Autowired
    AmountDistanceRepository amountDistanceRepository;
    @Autowired
    ShippingPointService shippingPointService;
    @Autowired
    OrderParentRepository orderParentRepository;
    @Autowired
    WhatsAppService whatsAppService;
    @Autowired
    DeliveryDriverRepository deliveryDriverRepository;
    @Autowired
    OrderParentRepository orderRepository;
    @Autowired
    ManagedPageCPLRepository managedPageCPLRepository;
    @Transactional
    @Override
    public Delivery transfromToDelivery(CallForTendersRequest request) {
        OrderParent orderParent = request.getOrderParent();
        orderParent.setDStatus(25);
        orderParentRepository.save(orderParent);
        Delivery delivery = new Delivery();
        delivery.setPhoneNumber(request.getOrderParent().getCustomerNumber());
        delivery.setOrderMotherId(request.getOrderParent().getIdOrderM());
        delivery.setShippingPointId(request.getShippingPointId());
        delivery.setStatus("CALL_FOR_TENDERED");
        delivery.setStartedAt(LocalDateTime.now());
        AmountDistance amountDistance = amountDistanceRepository.findByManagedPageId(request.getOrderParent().getIdManagedPages().longValue()).get(0);
        ShippingPoint shippingPoint = shippingPointService.getShippingPointById(request.getShippingPointId()).orElse(null);

        if(amountDistance!=null){
            delivery.setAmount(BigDecimal.valueOf(amountDistance.getPricePerDistance().doubleValue() * shippingPoint.getDistance().doubleValue()));
            delivery.setDistance(shippingPoint.getDistance().doubleValue());
        }
        delivery.setShippingAddress(shippingPoint.getPlaceName());
//        delivery.setAmount();
        deliveryRepository.save(delivery);
        return delivery;
    }

    @Override
    public ApiResponse makeAcall(Delivery delivery) {
        try {
            if (delivery == null) {
                log.error("Cannot make a call: delivery is null");
                ApiResponse response = new ApiResponse();
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setData("Delivery cannot be null");
                return response;
            }

            String phoneNumber = delivery.getPhoneNumber();
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                log.error("Cannot make a call: phone number is missing for delivery ID {}", delivery.getId());
                ApiResponse response = new ApiResponse();
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setData("Phone number is missing");
                return response;
            }

            // Format phone number if needed (ensure it has country code)
            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+" + phoneNumber;
            }

            // Send WhatsApp template message instead of regular message
            boolean sent = whatsAppService.sendHelloWorldTemplate(phoneNumber);

            if (sent) {
                log.info("WhatsApp template message sent successfully to {} for delivery ID {}", phoneNumber, delivery.getId());

                // Update delivery status
                delivery.setStatus("NOTIFIED_VIA_WHATSAPP_TEMPLATE");
                deliveryRepository.save(delivery);

                ApiResponse response = new ApiResponse();
                response.setStatus(HttpStatus.OK.value());
                response.setData("WhatsApp template message sent successfully");
                return response;
            } else {
                log.error("Failed to send WhatsApp template message to {} for delivery ID {}", phoneNumber, delivery.getId());
                ApiResponse response = new ApiResponse();
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.setData("Failed to send WhatsApp template message");
                return response;
            }
        } catch (Exception e) {
            log.error("Error sending WhatsApp template message for delivery ID {}", delivery.getId(), e);
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setData("Error sending WhatsApp template message: " + e.getMessage());
            return response;
        }
    }

    @Override
    public ApiResponse makeAcall(int deliveryId) {
        try {
            // Find delivery by ID
            Delivery delivery = deliveryRepository.findById((long) deliveryId).orElse(null);

            if (delivery == null) {
                log.error("Cannot make a call: delivery not found with ID {}", deliveryId);
                ApiResponse response = new ApiResponse();
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setData("Delivery not found with ID: " + deliveryId);
                return response;
            }

            return makeAcall(delivery);
        } catch (Exception e) {
            log.error("Error processing makeAcall for delivery ID {}", deliveryId, e);
            ApiResponse response = new ApiResponse();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setData("Error processing call: " + e.getMessage());
            return response;
        }
    }

    /**
     * Creates a message with delivery details.
     *
     * @param delivery The delivery entity
     * @return A formatted message string
     */
    private String createDeliveryMessage(Delivery delivery) {
        StringBuilder message = new StringBuilder();
        message.append("Hello! You have a new delivery notification.\n\n");
        message.append("Delivery ID: ").append(delivery.getId()).append("\n");

        if (delivery.getShippingAddress() != null && !delivery.getShippingAddress().isEmpty()) {
            message.append("Shipping Address: ").append(delivery.getShippingAddress()).append("\n");
        }

        if (delivery.getAmount() != null) {
            message.append("Amount: ").append(delivery.getAmount()).append("\n");
        }

        if (delivery.getDistance() > 0) {
            message.append("Distance: ").append(delivery.getDistance()).append(" km\n");
        }

        message.append("Status: ").append(delivery.getStatus()).append("\n");
        message.append("Started At: ").append(delivery.getStartedAt()).append("\n\n");
        message.append("Thank you for using our service!");

        return message.toString();
    }
    @Transactional
    @Override
    public ApiResponse sendTemplateMessage(Delivery delivery) {
        try {
            ApiResponse validationResponse = validateDelivery(delivery);
            if (validationResponse != null) return validationResponse;
            List<DeliveryDriver> deliveryDrivers =
                    deliveryDriverRepository.findByPriceInRange(delivery.getAmount().doubleValue());
            boolean sent = true;
            String message = "";
            OrderParent order = orderParentRepository.findById(delivery.getOrderMotherId()).orElse(null);
            assert order != null;
            ManagedPageCPL managedPageCPL = managedPageCPLRepository.findByIdMp(order.getIdManagedPages().longValue());
            for (int i = 0; i < deliveryDrivers.size(); i++) {
                String phoneNumber = formatPhoneNumber(deliveryDrivers.get(i).getPhoneNumber());
                if (phoneNumber == null) {
                    sent = false;
                    message = "Phone number is missing for delivery driver ID " + deliveryDrivers.get(i).getId();
                    break;
                }
                try {
                    whatsAppService.sendTemplateMessage (
                            phoneNumber,
                            "call_for_tenders",
                            "DEL_"+delivery.getId()+"_"+managedPageCPL.getIdMp()+"_"+managedPageCPL.getIdSeller(),
                            delivery.getId(),
                            delivery.getShippingAddress(),
                            delivery.getAmount(),
                            delivery.getDistance(),
                            delivery.getStartedAt().plusMinutes(50),
                            managedPageCPL.getPageTitle()
                    );
                } catch (Exception e) {
                    log.error("Error sending template message to {}: {}", phoneNumber, e.getMessage());
                    message = "Error sending template message to " + phoneNumber + ": " + e.getMessage();
                    break;
                }
            }
            if (sent) {
                message = "WhatsApp template message sent successfully to all eligible delivery drivers";
                log.info("Template message sent to for delivery {}", delivery.getId());
                return buildResponse(HttpStatus.OK, message);
            } else {
                log.error("Failed to send template message to for delivery {}", delivery.getId());
                return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private ApiResponse validateDelivery(Delivery delivery) {
        if (delivery == null) {
            log.error("Cannot send template message: delivery is null");
            return buildResponse(HttpStatus.BAD_REQUEST, "Delivery cannot be null");
        }
        return null;
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            log.error("Cannot send template message: phone number is missing");
            return null;
        }
        return phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
    }

    private ApiResponse buildResponse(HttpStatus status, String message) {
        ApiResponse response = new ApiResponse();
        response.setStatus(status.value());
        response.setData(message);
        return response;
    }
    @Transactional
    @Override
    public ApiResponse sendTemplateMessage(int deliveryId) {
        try {
            Delivery delivery = deliveryRepository.findById((long) deliveryId).orElse(null);
            if (delivery == null) {
                log.error("Cannot send template message: delivery not found with ID {}", deliveryId);
                ApiResponse response = new ApiResponse();
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setData("Delivery not found with ID: " + deliveryId);
                return response;
            }
            return sendTemplateMessage(delivery);
        } catch (Exception e) {
            log.error("Error processing sendTemplateMessage for delivery ID {}", deliveryId, e);
            throw e;
        }
    }
}
