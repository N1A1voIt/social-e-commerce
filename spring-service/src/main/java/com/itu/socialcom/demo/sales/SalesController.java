package com.itu.socialcom.demo.sales;

import com.itu.socialcom.demo.authentication.token.TokenV2Service;
import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.sales.dto.ImportResult;
import com.itu.socialcom.demo.sales.service.SalesCsvImportService;
import com.itu.socialcom.demo.sales.service.SalesFilterService;
import com.itu.socialcom.demo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class SalesController {
    
    @Autowired
    private SalesRepository salesRepository;
    
    @Autowired
    private TokenV2Service tokenV2Service;

    @Autowired
    private SalesCsvImportService salesCsvImportService;

    @Autowired
    private SalesFilterService salesFilterService;

    @GetMapping("/api/sales")
    public ResponseEntity<ApiResponse> getAllSales(
            @RequestHeader(name = "Authorization") String token,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "fromName", required = false) String fromName,
            @RequestParam(name = "orderId", required = false) String orderId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        try {
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to view sales")));
                return ResponseEntity.status(401).body(apiResponse);
            }
            
            // Use filter service to get sales with filters
            Page<Sales> salesPage = salesFilterService.findSalesWithFilters(
                seller.getId().intValue(),
                status,
                fromName,
                orderId,
                startDate,
                endDate,
                pageable
            );
            
            SalesToDisplay salesToDisplay = new SalesToDisplay();
            salesToDisplay.setSales(salesPage.getContent());
            salesToDisplay.setTotalSales((int) salesPage.getTotalElements());

            System.out.println("Sales size: " + salesPage.getContent().size());
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

    /**
     * Import sales data from CSV file.
     * 
     * @param token Authorization token
     * @param file CSV file to import
     * @return API response with import statistics
     */
    @PostMapping("/api/sales/import")
    public ResponseEntity<ApiResponse> importSalesFromCsv(
            @RequestHeader(name = "Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        try {
            // Validate authentication
            Seller seller = tokenV2Service.findSellerByToken(token).orElse(null);
            if (seller == null) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(401);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please log in to import sales")));
                return ResponseEntity.status(401).body(apiResponse);
            }

            // Validate file
            if (file.isEmpty()) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(400);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please select a CSV file to upload")));
                return ResponseEntity.status(400).body(apiResponse);
            }

            // Check file type
            String contentType = file.getContentType();
            String filename = file.getOriginalFilename();
            if (contentType == null || 
                (!contentType.equals("text/csv") && 
                 (filename == null || !filename.endsWith(".csv")))) {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setStatus(400);
                apiResponse.setData(null);
                apiResponse.setErrors(List.of(new Exception("Please upload a valid CSV file")));
                return ResponseEntity.status(400).body(apiResponse);
            }

            // Process the import
            ImportResult result = salesCsvImportService.importFromCsv(file, seller.getId());

            ApiResponse apiResponse = new ApiResponse();
            if (result.hasErrors() && result.getSuccessfulImports() == 0) {
                apiResponse.setStatus(400);
                apiResponse.setData(result);
                apiResponse.setErrors(result.getErrors().stream()
                    .map(Exception::new)
                    .toList());
                return ResponseEntity.status(400).body(apiResponse);
            }

            apiResponse.setStatus(200);
            apiResponse.setData(result);
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

