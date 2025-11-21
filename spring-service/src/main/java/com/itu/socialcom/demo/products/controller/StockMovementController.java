package com.itu.socialcom.demo.products.controller;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.products.model.StockMovement;
import com.itu.socialcom.demo.products.service.StockMovementService;
import com.itu.socialcom.demo.utils.ApiResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class StockMovementController {
    
    @Autowired
    private StockMovementService stockMovementService;
    
    @Autowired
    private TokenV2Service tokenV2Service;
    
    @GetMapping("/api/stock-movements")
    public ResponseEntity<ApiResponse> getStockMovements(
            @RequestHeader(name = "Authorization") String token,
            Pageable pageable,
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(name = "variantId", required = false) Long variantId,
            @RequestParam(name = "movementType", required = false) String movementType,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        try {
            // Handle both Bearer and non-Bearer token formats
            String cleanToken = token.startsWith("Bearer ") ? token.replace("Bearer ", "") : token;
            Seller seller = tokenV2Service.findSellerByToken(cleanToken).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view stock movements")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            // Parse date parameters
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startDate != null && !startDate.isEmpty()) {
                startDateTime = LocalDateTime.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                endDateTime = LocalDateTime.parse(endDate);
            }

            // Use multi-criteria search that combines all filters
            Page<StockMovement> movements = stockMovementService.searchStockMovementsWithFilters(
                seller.getId(), search, movementType, productId, variantId, 
                startDateTime, endDateTime, pageable);
            
            StockMovementPage stockMovementPage = new StockMovementPage();
            stockMovementPage.setContent(movements.getContent().toArray(new StockMovement[0]));
            stockMovementPage.setSize(movements.getSize());
            stockMovementPage.setNumber(movements.getNumber());
            stockMovementPage.setTotalElements(movements.getTotalElements());
            stockMovementPage.setTotalPages(movements.getTotalPages());
            stockMovementPage.setFirst(movements.isFirst());
            stockMovementPage.setLast(movements.isLast());
            
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(stockMovementPage);
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
    
    @GetMapping("/api/stock-movements/filters")
    public ResponseEntity<ApiResponse> getStockMovementFilters(@RequestHeader(name = "Authorization") String token) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view filters")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            Map<String, Object> filters = stockMovementService.getStockMovementFilters(seller.getId());
            
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(200);
            apiResponse.setData(filters);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(500);
            apiResponse.setData(null);
            apiResponse.setErrors(List.of(e));
            return ResponseEntity.status(500).body(apiResponse);
        }
    }
    
    @GetMapping("/api/stock-movements/export/csv")
    public ResponseEntity<byte[]> exportStockMovementsCsv(
            @RequestHeader(name = "Authorization") String token,
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(name = "variantId", required = false) Long variantId,
            @RequestParam(name = "movementType", required = false) String movementType,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        try {
            // Handle both Bearer and non-Bearer token formats
            String cleanToken = token.startsWith("Bearer ") ? token.replace("Bearer ", "") : token;
            Seller seller = tokenV2Service.findSellerByToken(cleanToken).orElse(null);
            if (seller == null) {
                return ResponseEntity.status(401).build();
            }

            // Parse date parameters with better error handling
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            try {
                if (startDate != null && !startDate.isEmpty()) {
                    startDateTime = LocalDateTime.parse(startDate);
                }
                if (endDate != null && !endDate.isEmpty()) {
                    endDateTime = LocalDateTime.parse(endDate);
                }
            } catch (Exception dateParseError) {
                // Log error but continue with null dates
                System.err.println("Date parsing error: " + dateParseError.getMessage());
            }

            byte[] csvData = stockMovementService.exportStockMovementsCsv(
                seller.getId(), search, movementType, productId, variantId, 
                startDateTime, endDateTime);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "stock-movements.csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/api/stock-movements/export/pdf")
    public ResponseEntity<byte[]> exportStockMovementsPdf(
            @RequestHeader(name = "Authorization") String token,
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(name = "variantId", required = false) Long variantId,
            @RequestParam(name = "movementType", required = false) String movementType,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        try {
            // Handle both Bearer and non-Bearer token formats
            String cleanToken = token.startsWith("Bearer ") ? token.replace("Bearer ", "") : token;
            Seller seller = tokenV2Service.findSellerByToken(cleanToken).orElse(null);
            if (seller == null) {
                return ResponseEntity.status(401).build();
            }

            // Parse date parameters with better error handling
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            try {
                if (startDate != null && !startDate.isEmpty()) {
                    startDateTime = LocalDateTime.parse(startDate);
                }
                if (endDate != null && !endDate.isEmpty()) {
                    endDateTime = LocalDateTime.parse(endDate);
                }
            } catch (Exception dateParseError) {
                // Log error but continue with null dates
                System.err.println("Date parsing error: " + dateParseError.getMessage());
            }

            byte[] pdfData = stockMovementService.exportStockMovementsPdf(
                seller.getId(), search, movementType, productId, variantId, 
                startDateTime, endDateTime);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "stock-movements.pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
@Data
class StockMovementPage {
    StockMovement[] content;
    long totalElements;
    int totalPages;
    int size;
    int number;
    boolean first;
    boolean last;
}
