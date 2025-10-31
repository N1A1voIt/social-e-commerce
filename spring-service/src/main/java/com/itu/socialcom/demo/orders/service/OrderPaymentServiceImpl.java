package com.itu.socialcom.demo.orders.service;

import com.itu.socialcom.demo.authentication.user.phonenumber.SellerPhoneNumber;
import com.itu.socialcom.demo.authentication.user.phonenumber.SellerPhoneNumberRepository;
import com.itu.socialcom.demo.moneytransactions.PaymentRequest;
import com.itu.socialcom.demo.moneytransactions.PaymentResponse;
import com.itu.socialcom.demo.moneytransactions.mvola.MVolaProvider;
import com.itu.socialcom.demo.orders.OrderChild;
import com.itu.socialcom.demo.orders.OrderParent;
import com.itu.socialcom.demo.orders.dto.PaymentDTO;
import com.itu.socialcom.demo.orders.repository.OrderChildRepository;
import com.itu.socialcom.demo.orders.repository.OrderParentRepository;
import com.itu.socialcom.demo.orders.tempLink.TempLink;
import com.itu.socialcom.demo.orders.tempLink.TempLinkRepository;
import com.itu.socialcom.demo.sales.Sales;
import com.itu.socialcom.demo.sales.SalesDetails;
import com.itu.socialcom.demo.sales.SalesRepository;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPagesNumber;
import com.itu.socialcom.demo.socialmedia.repository.ManagedPagesNumberRepository;
import com.itu.socialcom.demo.stocks.StockChild;
import com.itu.socialcom.demo.stocks.StockParent;
import com.itu.socialcom.demo.stocks.repository.StockChildRepository;
import com.itu.socialcom.demo.stocks.repository.StockParentRepository;
import com.itu.socialcom.demo.stocks.services.StockPersistanceService;
import com.itu.socialcom.demo.stocks.services.StockUpdatingService;
import jakarta.transaction.Transactional;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderPaymentServiceImpl implements OrderPaymentService{
    @Autowired
    private TempLinkRepository tempLinkRepository;
    @Autowired
    private OrderParentRepository parentRepository;
    @Autowired
    private OrderChildRepository orderChildRepository;
    @Autowired
    private ManagedPagesNumberRepository managedPagesNumberRepository;
    @Autowired
    private SellerPhoneNumberRepository sellerPhoneNumberRepository;
    @Autowired
    private MVolaProvider mVolaProvider;
    @Autowired
    private OrderParentRepository orderParentRepository;
    @Autowired
    private SalesRepository salesRepository;
    @Autowired
    private StockPersistanceService stockPersistanceService;
    @Override
    @Transactional
    public PaymentResponse processOrderPayment(PaymentDTO paymentDTO, String detailsIdentifier) throws Exception {
        try {

            TempLink tempLink = tempLinkRepository.findById(detailsIdentifier)
                    .orElseThrow(() -> new Exception("Invalid payment link."));
            if (tempLink.getUsed()) throw new Exception("This payment link has already been used.");
            OrderParent orderParent = parentRepository.findById(tempLink.getIdOrderM().longValue())
                    .orElseThrow(() -> new Exception("Order not found."));
            if (tempLink.getAmount() > Double.parseDouble(paymentDTO.getAmount())) {
                throw new Exception("Amount is too low.");
            }

            ManagedPagesNumber managedPagesNumber = new ManagedPagesNumber();
            if (orderParent.getIdManagedPages() != null) managedPagesNumber = managedPagesNumberRepository.findByIdMp(orderParent.getIdManagedPages().longValue());
            SellerPhoneNumber sellerPhoneNumber = new SellerPhoneNumber();
            if (orderParent.getIdManagedPages() != null) sellerPhoneNumber = sellerPhoneNumberRepository.findById(managedPagesNumber.getIdSpn())
                    .orElseThrow(() -> new Exception("Seller phone number not found."));
            if (orderParent.getIdManagedPages() == null) sellerPhoneNumber = sellerPhoneNumberRepository.findByIdSellerAndIdPm(orderParent.getIdSeller().longValue(),1L)
                    .orElseThrow(() -> new Exception("Seller phone number not found."));

            PaymentRequest paymentRequest = createPaymentRequest(paymentDTO,orderParent,sellerPhoneNumber);
            PaymentResponse paymentResponse = mVolaProvider.initiateTransaction(paymentRequest);
            orderParent.setDStatus(11); //Ordered
            tempLink.setUsed(true);
            orderParentRepository.save(orderParent);
            moveStock(orderParent);
            tempLinkRepository.save(tempLink);
            Sales sales = transformOrderToSale(orderParent, orderChildRepository.findByIdOrderM(orderParent.getIdOrderM().longValue()));
            return paymentResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Payment processing failed: " + e.getMessage());
        }
    }
    private StockParent moveStock(OrderParent parent) {
        List<OrderChild> orderChildren = orderChildRepository.findByIdOrderM(parent.getIdOrderM().longValue());
        List<StockChild> stockChildren = new ArrayList<>();
        for (OrderChild orderChild : orderChildren) {
            StockChild stockChild = new StockChild();
            stockChild.setPrice(orderChild.getPrice());
            stockChild.setProductName(orderChild.getProductName());
            stockChild.setVariantName(orderChild.getProductName());
//            stockChild.setDVariantNumber(orderChild.getQuantity());
            stockChild.setIdProduct(orderChild.getIdProduct());
            stockChild.setIdVariant(orderChild.getIdVariant());
            stockChild.setOutput(orderChild.getQuantity());
            stockChild.setInput(0.0);
            stockChild.setActionAt(LocalDateTime.now());
            stockChild.setCreatedAt(LocalDateTime.now());
            stockChildren.add(stockChild);
        }
        StockParent s = new StockParent();
        s.setCreatedAt(LocalDateTime.now());
        s.setDescription("Move of order "+parent.getDescription());
        s.setItems(stockChildren);
        s.setIdSeller(parent.getIdSeller().longValue());
        s.setIdOrderM(parent.getIdOrderM());
        s = stockPersistanceService.saveStock(s);
        return s;
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

    @Transactional
    public Sales transformOrderToSale(OrderParent orderParent, List<OrderChild> orderChildren) throws Exception {
        if (orderParent == null) {
            throw new Exception("OrderParent cannot be null");
        }

        // Create the Sales entity from OrderParent
        Sales sales = new Sales();

        // Map basic fields
        sales.setAmount(orderParent.getDTotal() != null
                ? BigDecimal.valueOf(orderParent.getDTotal())
                : BigDecimal.ZERO);
        sales.setEffectuatedAt(orderParent.getCreatedAt() != null
                ? orderParent.getCreatedAt()
                : LocalDateTime.now());
        sales.setFromNumber(orderParent.getCustomerNumber() != null
                ? orderParent.getCustomerNumber()
                : "");
        sales.setFromName(orderParent.getDCustomerName() != null
                ? orderParent.getDCustomerName()
                : "");

        sales.setDescription(orderParent.getDescription());
//        sales.setIdSpn(orderParent.getIdManagedPages() != null
//                ? orderParent.getIdManagedPages()
//                : 0);
        sales.setIdOrderM(orderParent.getIdOrderM() != null
                ? orderParent.getIdOrderM().intValue()
                : null);
        sales.setIdPc(orderParent.getIdPc() != null
                ? orderParent.getIdPc()
                : "");

        // Initialize the details list
        List<SalesDetails> salesDetailsList = new ArrayList<>();

        // Transform OrderChild items to SalesDetails
        if (orderChildren != null && !orderChildren.isEmpty()) {
            for (OrderChild orderChild : orderChildren) {
                SalesDetails salesDetail = new SalesDetails();

                salesDetail.setPrice(orderChild.getPrice() != null
                        ? BigDecimal.valueOf(orderChild.getPrice())
                        : BigDecimal.ZERO);
                salesDetail.setQuantity(orderChild.getQuantity() != null
                        ? BigDecimal.valueOf(orderChild.getQuantity())
                        : BigDecimal.ONE);
                salesDetail.setProductName(orderChild.getProductName());
                salesDetail.setVariantName(orderChild.getSku()); // Using SKU as variant name
                salesDetail.setIdProduct(orderChild.getIdProduct() != null
                        ? orderChild.getIdProduct().intValue()
                        : 0);
                salesDetail.setIdVariant(orderChild.getIdVariant() != null
                        ? orderChild.getIdVariant().intValue()
                        : 0);

                // Set the parent sales reference
                salesDetail.setSale(sales);
                salesDetailsList.add(salesDetail);
            }
        }

        // Set the details to the sales entity
        sales.setDetails(salesDetailsList);

        // Save the sales (cascade will save details automatically)
        return salesRepository.save(sales);
    }
}
