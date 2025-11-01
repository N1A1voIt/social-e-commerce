package com.itu.socialcom.demo.sales;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SalesController {
    
    @Autowired
    private SalesRepository salesRepository;
    
    @Autowired
    private TokenV2Service tokenV2Service;

    @GetMapping("/api/sales")
    public ResponseEntity<ApiResponse> getAllSales(@RequestHeader(name = "Authorization") String token, Pageable pageable) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view sales")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            
            SalesToDisplay salesToDisplay = new SalesToDisplay();
            List<Sales> sales = salesRepository.findByIdSeller(seller.getId().intValue(), pageable).getContent();
            salesToDisplay.setSales(sales);
            salesToDisplay.setTotalSales(salesRepository.countByIdSeller(seller.getId().intValue()));

            System.out.println(sales.size());
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(salesToDisplay);
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
    @GetMapping("/api/sales/paid/{id}")
    public ResponseEntity<ApiResponse> transformStatus(@RequestHeader(name = "Authorization") String token, @PathVariable(name = "id") Integer id) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view sales")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            Sales sales = salesRepository.findById(id).orElseThrow(() -> new RuntimeException("No sales with this id"));
            sales.setPaidAmount(sales.getAmount().doubleValue());
            sales.setStatus(11);
            salesRepository.save(sales);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(sales);
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

