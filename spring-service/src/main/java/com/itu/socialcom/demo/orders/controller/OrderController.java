package com.itu.socialcom.demo.orders.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.delivery.entity.Delivery;
import com.itu.socialcom.demo.orders.DownPayment;
import com.itu.socialcom.demo.orders.OrderChild;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.OrdersToDisplay;
import com.itu.socialcom.demo.orders.delivery.CallForTenderServiceImpl;
import com.itu.socialcom.demo.orders.dto.CallForTendersRequest;
import com.itu.socialcom.demo.orders.dto.MessageOrdering;
import com.itu.socialcom.demo.orders.repository.DownPaymentRepository;
import com.itu.socialcom.demo.orders.repository.OrderChildRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.orders.service.CreateOrderFromMessage;
import com.itu.socialcom.demo.orders.service.OrderCreationService;
import com.itu.socialcom.demo.orders.service.OrderPaymentLink;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<ApiResponse> getAllOrders(@RequestHeader(name = "Authorization") String token, Pageable pageable) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view orders")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            OrdersToDisplay ordersToDisplay = new OrdersToDisplay();
            List<OrderParent> orders = orderParentRepository.findAllByIdSeller(seller.getId().intValue(),pageable).getContent();
            DownPayment downPayment = downPaymentRepository.findByIdSeller(seller.getId()).get(0);
            for (OrderParent order : orders) {
                order.setDownP(downPayment.getPaymentInPercent() / 100 * order.getDTotal());
                order.setDownPPercent(downPayment.getPaymentInPercent() / 100);
            }
            ordersToDisplay.setOrders(orders);
            ordersToDisplay.setTotalOrders(orderParentRepository.countByIdSeller(seller.getId().intValue()));
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
}
