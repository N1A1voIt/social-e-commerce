package com.itu.socialcom.demo.orders.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.delivery.repository.DeliveryRepository;
import com.itu.socialcom.demo.moneytransactions.PaymentResponse;
import com.itu.socialcom.demo.orders.*;
import com.itu.socialcom.demo.orders.delivery.CallForTenderServiceImpl;
import com.itu.socialcom.demo.orders.deliveryapplicants.DeliveryApplicant;
import com.itu.socialcom.demo.orders.deliveryapplicants.DeliveryApplicantRepository;
import com.itu.socialcom.demo.orders.dto.CallForTendersRequest;
import com.itu.socialcom.demo.orders.dto.MessageOrdering;
import com.itu.socialcom.demo.orders.dto.PaymentDTO;
import com.itu.socialcom.demo.orders.dto.RefundRequest;
import com.itu.socialcom.demo.orders.repository.DownPaymentRepository;
import com.itu.socialcom.demo.orders.repository.OrderChildRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.orders.service.*;
import com.itu.socialcom.demo.orders.tempLink.TempLink;
import com.itu.socialcom.demo.orders.tempLink.TempLinkRepository;
import com.itu.socialcom.demo.utils.ApiResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {
    @Autowired
    private OrderCreationService orderCreationService;
    @Autowired
    private CreateOrderFromMessage createOrderFromMessage;
    @Autowired
    private OrderParentRepository orderParentRepository;
    @Autowired
    private OrderChildRepository orderChildRepository;
    @Autowired
    private TokenV2Service tokenV2Service;
    @Autowired
    private DownPaymentRepository downPaymentRepository;
    @Autowired
    private OrderPaymentLink orderPaymentLink;
    @Autowired
    private CallForTenderServiceImpl call;
    @Autowired
    private DeliveryApplicantRepository deliveryApplicantRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private OrderPaymentServiceImpl orderPaymentService;
    @Autowired
    private OrderFilterService orderFilterService;
    @Autowired
    private OrderDeliveredService orderDeliveredService;
    @Autowired
    private TempLinkRepository tempLinkRepository;
    @Autowired
    private CustomerPickupService customerPickupService;

    @PostMapping("/api/orders/save")
    public ResponseEntity<ApiResponse> createOrder(@RequestBody OrderParent orderParent,@RequestHeader(name = "Authorization") String token) {
        orderCreationService = createOrderFromMessage;
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to create an order")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            OrderParent createdOrder = orderCreationService.createOrder(orderParent,seller);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(createdOrder);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @GetMapping("/api/orders")
    public ResponseEntity<ApiResponse> getAllOrders(
            @RequestHeader(name = "Authorization") String token, 
            Pageable pageable,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "customerName", required = false) String customerName,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "idPc", required = false) String idPc) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view orders")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            
            // Parse date parameters
            java.time.LocalDateTime startDateTime = null;
            java.time.LocalDateTime endDateTime = null;
            
            if (startDate != null && !startDate.isEmpty()) {
                startDateTime = java.time.LocalDateTime.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                endDateTime = java.time.LocalDateTime.parse(endDate);
            }
            
            // Clean up customerName - treat empty strings as null
            String cleanCustomerName = (customerName != null && !customerName.trim().isEmpty()) ? customerName.trim() : null;
            String cleanIdPc = (idPc != null && !idPc.trim().isEmpty()) ? idPc.trim() : null;
            
            OrdersToDisplay ordersToDisplay = new OrdersToDisplay();
            
            // Use EntityManager-based filtering for flexible query building
            Page<OrderParent> orderPage = orderFilterService.findOrdersWithFilters(
                seller.getId().intValue(),
                status,
                cleanCustomerName,
                startDateTime,
                endDateTime,
                cleanIdPc,
                pageable
            );
            
            List<OrderParent> orders = orderPage.getContent();
            int totalOrders = (int) orderPage.getTotalElements();
            
            DownPayment downPayment = downPaymentRepository.findByIdSeller(seller.getId()).get(0);
            for (OrderParent order : orders) {
                order.setDownP(downPayment.getPaymentInPercent() / 100 * order.getDTotal());
                order.setDownPPercent(downPayment.getPaymentInPercent() / 100);
            }
            ordersToDisplay.setOrders(orders);
            ordersToDisplay.setTotalOrders(totalOrders);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(ordersToDisplay);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @GetMapping("/api/orders/{id}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable("id") Long id, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view order details")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            List<OrderChild> order = orderChildRepository.findByIdOrderM(id);
            if (order == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(404);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Order not found")));
                return ResponseEntity.status(404).body(apiResponse);
            }
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(order);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @PostMapping("/api/orders/save-from-message")
    public ResponseEntity<ApiResponse> createOrderFromMessage(@RequestBody MessageOrdering orderParent, @RequestHeader(name = "Authorization") String token) {
        orderCreationService = createOrderFromMessage;
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to create an order")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            OrderParent createdOrder = orderCreationService.createOrderFromMessage(orderParent,seller);

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(createdOrder);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @PostMapping("/api/order/ask-for-pay")
    public ResponseEntity<ApiResponse> payOrder(@RequestBody OrderParent orderParent, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to pay for an order")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            orderPaymentLink.askUserToPay(orderParent);
//            orderParentRepository.save(orderParent);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(orderParent);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }
    @Transactional
    @PostMapping("/api/order/call-for-tenders")
    public ResponseEntity<ApiResponse> callForTenders(@RequestBody CallForTendersRequest call, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to call for tenders")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            System.out.println(call.toString());
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
//            orderPaymentLink.callForTenders(call.getOrderParent(),call.getSupplierIds());
//            orderPaymentLink.callForTenders(orderParent);
            Delivery delivery = this.call.transfromToDelivery(call);
            apiResponse = this.call.sendTemplateMessage(delivery.getId().intValue());
            apiResponse.setData(delivery);
            return ResponseEntity.ok(apiResponse);
        }catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }
    @GetMapping("/api/order/call-for-tenders/test/{id_delivery}")
    public ResponseEntity<ApiResponse> testCallForTenders(@PathVariable("id_delivery") int idDelivery) {
        try {
//            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
//            if (seller == null) {
//                ApiResponse apiResponse = new ApiResponse();
//                apiResponse.setStatus(401);
//                apiResponse.setData(null);
//                apiResponse.setErrors(List.of(new Exception("Please log in to call for tenders")));
//                return ResponseEntity.status(401).body(apiResponse);
//            }
            ApiResponse apiResponse = call.sendTemplateMessage(idDelivery);
            return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
        } catch (Exception e) {
//            throw new RuntimeException(e);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @PostMapping("/api/order/pay")
    public ResponseEntity<ApiResponse> payOrderDirectly(@RequestBody PaymentDTO paymentDTO, @RequestParam(name = "link_identifier") String linkIdentifier) {
        try {
//            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
//            if (seller == null) {
//                ApiResponse apiResponse = new ApiResponse();
//                apiResponse.setStatus(401);
//                apiResponse.setData(null);
//                apiResponse.setErrors(List.of(new Exception("Please log in to pay for an order")));
//                return ResponseEntity.status(401).body(apiResponse);
//            }
            PaymentResponse paymentResponse = orderPaymentService.processOrderPayment(paymentDTO,linkIdentifier);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(paymentResponse);
            apiResponse.setErrors(null);
            return ResponseEntity.status(200).body(apiResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }


    @PostMapping("/api/order/pay-full-amount")
    public ResponseEntity<ApiResponse> payFullOrderDirectly(@RequestBody PaymentDTO paymentDTO, @RequestParam(name = "link_identifier") String linkIdentifier) {
        try {
//            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
//            if (seller == null) {
//                ApiResponse apiResponse = new ApiResponse();
//                apiResponse.setStatus(401);
//                apiResponse.setData(null);
//                apiResponse.setErrors(List.of(new Exception("Please log in to pay for an order")));
//                return ResponseEntity.status(401).body(apiResponse);
//            }
            PaymentResponse paymentResponse = orderPaymentService.processFullOrderPayment(paymentDTO,linkIdentifier);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(paymentResponse);
            apiResponse.setErrors(null);
            return ResponseEntity.status(200).body(apiResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }


    @GetMapping("/api/applications/{id_order}")
    public ResponseEntity<ApiResponse> applicantsList(@PathVariable("id_order") Long idOrder,@RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view applicants")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            Delivery delivery = deliveryRepository.findByOrderMotherId(idOrder).get(0);
            List<DeliveryApplicant> applicants = deliveryApplicantRepository.findBydStatusAndIdDelivery("CALL_FOR_TENDERED",delivery.getId());
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(applicants);
            return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @Autowired
    OrderRefundService orderRefundService;

    @PostMapping("/api/order/cancel")
    @Transactional
    public ResponseEntity<ApiResponse> refundOrder(@RequestBody RefundRequest refundRequest, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to refund an order")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            OrderParent orderParent = orderParentRepository.findById(refundRequest.getOrderId().longValue()).orElse(null);
            if (orderParent == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(404);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Order not found")));
                return ResponseEntity.status(404).body(apiResponse);
            }
            Refund refund = orderRefundService.cancelOrder(refundRequest);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(refund);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @GetMapping("/api/order/{id}")
    public ResponseEntity<ApiResponse> getOrder(@PathVariable("id") Long id) {
        try {
            OrderParent orderParent = orderParentRepository.findById(id).orElse(null);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(orderParent);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }
    @GetMapping("/api/order/complete-delivery/{id}")
    public ResponseEntity<ApiResponse> notifyCustomer(@PathVariable("id") Long id) {
        try {
            orderDeliveredService.notifyCustomer(id);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(null);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @GetMapping("/api/order/ask-for-full-payment")
    public ResponseEntity<ApiResponse> askForFullPayment(@RequestParam("id") Long id) {
        try {
            OrderParent orderParent = orderParentRepository.findById(id).orElse(null);
            orderParent = orderPaymentLink.askForUserToPayTheRest(orderParent);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(orderParent);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @PostMapping("/api/order/confirm-payment")
    public ResponseEntity<ApiResponse> confirmPayment(@RequestBody java.util.Map<String, Object> payload, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to confirm payment")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            Long orderId = Long.valueOf(payload.get("orderId").toString());
            String paymentMethod = payload.get("paymentMethod").toString();

            OrderParent orderParent = orderParentRepository.findById(orderId).orElse(null);
            if (orderParent == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(404);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Order not found")));
                return ResponseEntity.status(404).body(apiResponse);
            }

            // Update order status based on payment method
            // Status 41 -> Status 5 (Completed)
            if (orderParent.getDStatus() == 41) {
                orderParent.setDStatus(5); // Mark as completed
                orderParentRepository.save(orderParent);

                // Log the payment method used
                System.out.println("Payment confirmed for order " + orderId + " via " + paymentMethod);
            }

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(orderParent);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @PostMapping("/api/order/ask-for-full-payment-link")
    public ResponseEntity<ApiResponse> sendFullPaymentLink(@RequestBody OrderParent orderParent, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to send payment link")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            // Send full payment link via MVola
            orderPaymentLink.askForUserToPayTheRest(orderParent);
            
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(orderParent);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }



    @PostMapping("/api/order/process-cash-payment")
    public ResponseEntity<ApiResponse> processCashPayment(@RequestBody java.util.Map<String, Object> payload, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to process payment")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            Long orderId = Long.valueOf(payload.get("orderId").toString());
            OrderParent orderParent = orderParentRepository.findById(orderId).orElse(null);
            
            if (orderParent == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(404);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Order not found")));
                return ResponseEntity.status(404).body(apiResponse);
            }


            if (orderParent.getDStatus() == 41) {
                orderPaymentService.processCashPayment(orderParent);
            }

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(orderParent);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @GetMapping("/api/temp-link/{linkId}")
    public ResponseEntity<ApiResponse> getTempLinkById(@PathVariable("linkId") String linkId) {
        try {
            TempLink tempLink = tempLinkRepository.findById(linkId).orElse(null);
            
            if (tempLink == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(404);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Payment link not found")));
                return ResponseEntity.status(404).body(apiResponse);
            }

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(tempLink);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    /**
     * Set order to customer pickup mode (status 26)
     * Creates a sale with paid amount = 0
     */
    @PostMapping("/api/order/customer-pickup")
    @Transactional
    public ResponseEntity<ApiResponse> setCustomerPickup(@RequestBody java.util.Map<String, Object> payload, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to process order")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            Long orderId = Long.valueOf(payload.get("orderId").toString());
            OrderParent orderParent = customerPickupService.setCustomerPickup(orderId);

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(orderParent);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    /**
     * Complete customer pickup order (status 26 -> 51)
     * Creates cash payment for the full amount
     */
    @PostMapping("/api/order/complete-pickup")
    @Transactional
    public ResponseEntity<ApiResponse> completeCustomerPickup(@RequestBody java.util.Map<String, Object> payload, @RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to process order")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            Long orderId = Long.valueOf(payload.get("orderId").toString());
            OrderParent orderParent = customerPickupService.completeCustomerPickup(orderId);

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(orderParent);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }

}
