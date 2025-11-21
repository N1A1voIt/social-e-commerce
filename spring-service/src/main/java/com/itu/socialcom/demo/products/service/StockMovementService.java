package com.itu.socialcom.demo.products.service;

import com.itu.socialcom.demo.products.model.StockMovement;
import com.itu.socialcom.demo.products.repository.StockMovementRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockMovementService {
    
    @Autowired
    private StockMovementRepository stockMovementRepository;
    
    public Page<StockMovement> getStockMovementsBySeller(Long idSeller, Pageable pageable) {
        return stockMovementRepository.findByIdSellerOrderByActionAtDesc(idSeller, pageable);
    }
    
    public Page<StockMovement> getStockMovementsByProduct(Long idSeller, Long idProduct, Pageable pageable) {
        return stockMovementRepository.findByIdSellerAndIdProductOrderByActionAtDesc(idSeller, idProduct, pageable);
    }
    
    public Page<StockMovement> getStockMovementsByVariant(Long idSeller, Long idVariant, Pageable pageable) {
        return stockMovementRepository.findByIdSellerAndIdVariantOrderByActionAtDesc(idSeller, idVariant, pageable);
    }
    
    public Page<StockMovement> getStockMovementsByDateRange(Long idSeller, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return stockMovementRepository.findByIdSellerAndActionAtBetween(idSeller, startDate, endDate, pageable);
    }
    
    public Page<StockMovement> getStockMovementsByType(Long idSeller, String movementType, Pageable pageable) {
        return stockMovementRepository.findByIdSellerAndMovementType(idSeller, movementType, pageable);
    }
    
    public Page<StockMovement> searchStockMovements(Long idSeller, String searchTerm, Pageable pageable) {
        return stockMovementRepository.findByIdSellerAndSearchTerm(idSeller, searchTerm, pageable);
    }
    
    public Page<StockMovement> searchStockMovementsWithFilters(
            Long idSeller, String search, String movementType, Long productId, 
            Long variantId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        
        return stockMovementRepository.findByMultipleCriteria(
            idSeller, search, movementType, productId, variantId, startDate, endDate, pageable);
    }
    
    public Map<String, Object> getStockMovementFilters(Long idSeller) {
        Map<String, Object> filters = new HashMap<>();
        
        // Get available movement types
        String[] movementTypes = {"STOCK_IN", "STOCK_OUT", "ADJUSTMENT"};
        filters.put("movementTypes", movementTypes);
        
        return filters;
    }
    
    public byte[] exportStockMovementsCsv(
            Long idSeller, String search, String movementType, Long productId, 
            Long variantId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<StockMovement> movements = stockMovementRepository.findAllByMultipleCriteria(
                idSeller, search, movementType, productId, variantId, startDate, endDate);
            
            System.out.println("Exporting " + movements.size() + " stock movements to CSV for seller " + idSeller);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);
            
            // CSV Header
            writer.println("Date,Time,Product,SKU,Variant,Movement Type,Net Movement,Price,Product Stock After,Variant Stock After,Order ID,Customer");
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            
            // CSV Data
            for (StockMovement movement : movements) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    movement.getActionAt() != null ? movement.getActionAt().format(dateFormatter) : "",
                    movement.getActionAt() != null ? movement.getActionAt().format(timeFormatter) : "",
                    escapeQuotes(movement.getProductName()),
                    escapeQuotes(movement.getSkuPrefix()),
                    escapeQuotes(movement.getVariantName()),
                    movement.getMovementType() != null ? movement.getMovementType() : "",
                    formatNumber(movement.getNetMovement()),
                    formatPrice(movement.getPrice()),
                    formatNumber(movement.getProductStockAfter()),
                    formatNumber(movement.getVariantStockAfter()),
                    movement.getIdOrderM() != null ? "ORD" + movement.getIdOrderM() : "",
                    escapeQuotes(movement.getCustomerName())
                );
            }
            
            writer.flush();
            writer.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV export", e);
        }
    }
    
    public byte[] exportStockMovementsPdf(
            Long idSeller, String search, String movementType, Long productId, 
            Long variantId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<StockMovement> movements = stockMovementRepository.findAllByMultipleCriteria(
                idSeller, search, movementType, productId, variantId, startDate, endDate);
            
            System.out.println("Exporting " + movements.size() + " stock movements to PDF for seller " + idSeller);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 54, 54);
            PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Stock Movements Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Generated date
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);
            Paragraph dateInfo = new Paragraph("Generated on: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), normalFont);
            dateInfo.setAlignment(Element.ALIGN_CENTER);
            dateInfo.setSpacingAfter(20);
            document.add(dateInfo);
            
            if (movements.isEmpty()) {
                Paragraph noData = new Paragraph("No stock movements found for the selected criteria.", normalFont);
                noData.setAlignment(Element.ALIGN_CENTER);
                document.add(noData);
            } else {
                // Table
                PdfPTable table = new PdfPTable(8);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10);
                
                // Set column widths
                float[] columnWidths = {12f, 12f, 20f, 15f, 12f, 8f, 10f, 11f};
                table.setWidths(columnWidths);
                
                // Table headers
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
                String[] headers = {"Date", "Time", "Product", "Variant", "Type", "Movement", "Price", "Order"};
                
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(5);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }
                
                // Table data
                Font dataFont = new Font(Font.FontFamily.HELVETICA, 8);
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                
                for (StockMovement movement : movements) {
                    // Date
                    table.addCell(new PdfPCell(new Phrase(
                        movement.getActionAt() != null ? movement.getActionAt().format(dateFormatter) : "", dataFont)));
                    
                    // Time
                    table.addCell(new PdfPCell(new Phrase(
                        movement.getActionAt() != null ? movement.getActionAt().format(timeFormatter) : "", dataFont)));
                    
                    // Product
                    String productText = (movement.getProductName() != null ? movement.getProductName() : "") +
                        (movement.getSkuPrefix() != null ? " (" + movement.getSkuPrefix() + ")" : "");
                    table.addCell(new PdfPCell(new Phrase(productText, dataFont)));
                    
                    // Variant
                    table.addCell(new PdfPCell(new Phrase(
                        movement.getVariantName() != null ? movement.getVariantName() : "", dataFont)));
                    
                    // Type
                    table.addCell(new PdfPCell(new Phrase(
                        movement.getMovementType() != null ? movement.getMovementType() : "", dataFont)));
                    
                    // Movement
                    String netMovement = formatNumber(movement.getNetMovement());
                    if (movement.getNetMovement() != null && movement.getNetMovement().compareTo(BigDecimal.ZERO) > 0) {
                        netMovement = "+" + netMovement;
                    }
                    table.addCell(new PdfPCell(new Phrase(netMovement, dataFont)));
                    
                    // Price
                    table.addCell(new PdfPCell(new Phrase(
                        movement.getPrice() != null ? "$" + formatPrice(movement.getPrice()) : "", dataFont)));
                    
                    // Order
                    String orderInfo = "";
                    if (movement.getIdOrderM() != null) {
                        orderInfo = "ORD" + movement.getIdOrderM();
                        if (movement.getCustomerName() != null && !movement.getCustomerName().trim().isEmpty()) {
                            orderInfo += "\n" + movement.getCustomerName();
                        }
                    }
                    table.addCell(new PdfPCell(new Phrase(orderInfo, dataFont)));
                }
                
                document.add(table);
                
                // Summary
                Paragraph summary = new Paragraph("\nTotal Records: " + movements.size(), normalFont);
                summary.setSpacingBefore(20);
                document.add(summary);
            }
            
            document.close();
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF export: " + e.getMessage(), e);
        }
    }
    
    private String escapeQuotes(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"");
    }
    
    private String formatNumber(BigDecimal value) {
        return value != null ? value.toString() : "0";
    }
    
    private String formatPrice(Number value) {
        if (value == null) return "0.00";
        return String.format("%.2f", value.doubleValue());
    }
}