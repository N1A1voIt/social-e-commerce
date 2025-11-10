package com.itu.socialcom.demo.sales.service;

import com.itu.socialcom.demo.potentialCustomers.entity.PotentialCustomerV2;
import com.itu.socialcom.demo.potentialCustomers.repository.PotentialCustomerV2Repository;
import com.itu.socialcom.demo.products.model.Product;
import com.itu.socialcom.demo.products.repository.ProductRepository;
import com.itu.socialcom.demo.products.variants.model.Variant;
import com.itu.socialcom.demo.products.variants.repository.VariantRepository;
import com.itu.socialcom.demo.sales.Sales;
import com.itu.socialcom.demo.sales.SalesDetails;
import com.itu.socialcom.demo.sales.SalesDetailsRepository;
import com.itu.socialcom.demo.sales.SalesRepository;
import com.itu.socialcom.demo.sales.dto.ImportResult;
import com.itu.socialcom.demo.sales.dto.SalesCsvRow;
import com.itu.socialcom.demo.socialmedia.entity.SupportedPlatform;
import com.itu.socialcom.demo.socialmedia.repository.SupportedPlatformRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service for importing sales data from CSV files.
 * Handles parsing, validation, and database persistence.
 */
@Service
public class SalesCsvImportService {

    private static final Logger logger = LoggerFactory.getLogger(SalesCsvImportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private SalesDetailsRepository salesDetailsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private PotentialCustomerV2Repository potentialCustomerRepository;

    @Autowired
    private SupportedPlatformRepository supportedPlatformRepository;

    /**
     * Import sales data from a CSV file.
     *
     * @param file     the CSV file to import
     * @param sellerId the ID of the seller importing the data
     * @return ImportResult containing statistics and any errors
     */
    @Transactional
    public ImportResult importFromCsv(MultipartFile file, Long sellerId) {
        ImportResult result = new ImportResult();

        try {
            List<SalesCsvRow> csvRows = parseCsvFile(file);
            result.setTotalRows(csvRows.size());

            // Group rows by sale ID
            Map<Integer, List<SalesCsvRow>> salesGroups = groupBySaleId(csvRows);
//            List<PotentialCustomerV2> potentialCustomerV2s = new ArrayList<>();
            List<Product> products = productRepository.findByIdSeller(sellerId.intValue(), PageRequest.of(0, 100)).stream().toList();
            List<Variant> variants = variantRepository.findByIdSeller(sellerId);
            for (Map.Entry<Integer, List<SalesCsvRow>> entry : salesGroups.entrySet()) {
                try {
                    processSale(entry.getValue(), sellerId, result,products,variants);
                } catch (Exception e) {
                    result.addError("Error processing sale ID " + entry.getKey() + ": " + e.getMessage());
                    logger.error("Error processing sale ID {}", entry.getKey(), e);
                }
            }

            result.setSuccessfulImports(result.getTotalRows() - result.getFailedImports());

        } catch (Exception e) {
            result.addError("Failed to parse CSV file: " + e.getMessage());
            logger.error("Failed to import CSV", e);
        }

        return result;
    }

    /**
     * Parse the CSV file and convert rows to SalesCsvRow objects.
     */
    private List<SalesCsvRow> parseCsvFile(MultipartFile file) throws Exception {
        List<SalesCsvRow> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            reader.readLine(); // Skip header
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    SalesCsvRow row = parseCsvLine(line);
                    rows.add(row);
                } catch (Exception e) {
                    logger.warn("Failed to parse line {}: {}", lineNumber, e.getMessage());
                    throw new Exception("Error at line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        return rows;
    }

    /**
     * Parse a single CSV line into a SalesCsvRow object.
     */
    private SalesCsvRow parseCsvLine(String line) throws Exception {
        // Split by comma, handling quoted fields
        List<String> fields = splitCsvLine(line);

        if (fields.size() != 14) {
            throw new Exception("Invalid number of columns. Expected 14, got " + fields.size());
        }

        SalesCsvRow row = new SalesCsvRow();

        try {
            row.setIdSale(Integer.parseInt(fields.get(0)));
            row.setAmount(new BigDecimal(fields.get(1)));
            row.setEffectuedAt(LocalDateTime.parse(fields.get(2), DATE_FORMATTER));
            row.setFromNumber(fields.get(3));
            row.setFromName(fields.get(4));
            row.setDescription(fields.get(5));
            row.setPlatform(fields.get(6));
            row.setIdOrderM(fields.get(7));
            row.setPrice(new BigDecimal(fields.get(8)));
            row.setQuantity(Integer.parseInt(fields.get(9)));
            row.setProductName(fields.get(10));
            row.setVariantName(fields.get(11));
            row.setSkuProduct(fields.get(12));
            row.setSkuVariant(fields.get(13));
        } catch (NumberFormatException e) {
            throw new Exception("Invalid number format: " + e.getMessage());
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid date format: " + e.getMessage());
        }

        return row;
    }

    /**
     * Split CSV line handling quoted fields.
     */
    private List<String> splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString().trim());

        return fields;
    }

    /**
     * Group CSV rows by sale ID.
     */
    private Map<Integer, List<SalesCsvRow>> groupBySaleId(List<SalesCsvRow> rows) {
        Map<Integer, List<SalesCsvRow>> groups = new LinkedHashMap<>();

        for (SalesCsvRow row : rows) {
            groups.computeIfAbsent(row.getIdSale(), k -> new ArrayList<>()).add(row);
        }

        return groups;
    }

    /**
     * Process a single sale and its details.
     */
    private void processSale(List<SalesCsvRow> rows, Long sellerId, ImportResult result,List<Product> products,List<Variant> variants) {
        if (rows.isEmpty()) {
            return;
        }

        SalesCsvRow firstRow = rows.get(0);

        // Find or create potential customer
        PotentialCustomerV2 customer = findOrCreateCustomer(firstRow, result);
        if (customer == null) {
            result.addError("Failed to create/find customer for sale ID " + firstRow.getIdSale());
            return;
        }

        // Create the sale
        Sales sale = new Sales();
        sale.setAmount(firstRow.getAmount());
        sale.setEffectuatedAt(firstRow.getEffectuedAt());
        sale.setFromNumber(firstRow.getFromNumber());
        sale.setFromName(firstRow.getFromName());
        sale.setDescription(firstRow.getDescription());
        sale.setIdSeller(sellerId.intValue());
        sale.setIdPc(customer.getId());

        // Set default values for required fields
        sale.setIdOrderM(firstRow.getIdOrderM().isEmpty() ? null : Integer.parseInt(firstRow.getIdOrderM()));
        sale.setIdSpn(1); // Default phone number ID - adjust as needed
        sale.setStatus(1); // Default status
        sale.setPaidAmount(0.0);

        try {
            sale = salesRepository.save(sale);
            result.setSalesCreated(result.getSalesCreated() + 1);

            // Process each detail
            for (SalesCsvRow row : rows) {
                try {
                    SalesDetails detail = createSalesDetail(row, sale, sellerId,products,variants);
                    salesDetailsRepository.save(detail);
                    result.setSalesDetailsCreated(result.getSalesDetailsCreated() + 1);
                } catch (Exception e) {
                    result.addWarning("Failed to create detail for sale " + sale.getIdSale() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addError("Failed to save sale " + firstRow.getIdSale() + ": " + e.getMessage());
        }
    }

    /**
     * Find or create a potential customer based on the CSV data.
     */
    private PotentialCustomerV2 findOrCreateCustomer(SalesCsvRow row, ImportResult result) {
        try {
            // Try to find existing customer by phone number
//            Optional<PotentialCustomerV2> existingCustomer =
//                potentialCustomerRepository.findFirstByIdentifierOnPlatform(row.getFromNumber());
//
//            if (existingCustomer.isPresent()) {
//                return existingCustomer.get();
//            }

            // Create new customer
            PotentialCustomerV2 customer = new PotentialCustomerV2();
            customer.setName(row.getFromName());
            customer.setIdentifierOnPlatform(row.getFromNumber());
            customer.setPlatform(row.getPlatform());
//            customer.setId("Imported customer : " + UUID.randomUUID().toString());

            // Find platform ID
//            Optional<SupportedPlatform> platform =
//                supportedPlatformRepository.findByLabel(row.getPlatform().toLowerCase());

            if (row.getPlatform() != null) {
                if (row.getPlatform().equalsIgnoreCase("facebook")) customer.setSupportedPlatform(1L);
                if (row.getPlatform().equalsIgnoreCase("instagram")) customer.setSupportedPlatform(2L);
            } else {
                customer.setSupportedPlatform(1L);
                result.addWarning("Platform '" + row.getPlatform() + "' not found, using default");
            }

            return potentialCustomerRepository.save(customer);
        } catch (Exception e) {
            logger.error("Failed to create customer", e);
            return null;
        }
    }

    /**
     * Create a sales detail from CSV row.
     */
    private SalesDetails createSalesDetail(SalesCsvRow row, Sales sale, Long sellerId,List<Product> products,List<Variant> variants) {
        SalesDetails detail = new SalesDetails();
        detail.setSale(sale);
        detail.setPrice(row.getPrice());
        detail.setQuantity(new BigDecimal(row.getQuantity()));
        detail.setProductName(row.getProductName());
        detail.setVariantName(row.getVariantName());

        Long productId = null;
        for (Product product : products) {
            if(product.getSkuPrefix().equals(row.getSkuProduct())) {
                productId = product.getIdProduct();
                break;
            }
        }

        Long variantId = null;
        for (Variant variant : variants) {
            if(variant.getSku().equals(row.getSkuVariant())) {
                variantId = variant.getIdVariant();
                break;
            }
        }

        detail.setIdProduct(productId != null ? productId.intValue() : 1);
        detail.setIdVariant(variantId != null ? variantId.intValue() : 1);

        return detail;
    }
}

