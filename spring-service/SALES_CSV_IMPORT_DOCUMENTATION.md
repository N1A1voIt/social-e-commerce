# Sales CSV Import Feature Documentation

## Overview
This feature allows sellers to import sales data from CSV files. The system automatically creates sales records, sales details, and associated customers from the CSV data.

## CSV Format

### Required Columns (14 columns total)
The CSV file must have the following columns in order:

1. **id_sale** - Sale ID (Integer)
2. **amount** - Total sale amount (Decimal)
3. **effectued_at** - Sale date and time (Format: `yyyy-MM-dd HH:mm`)
4. **from_number** - Customer phone number (Text)
5. **from_name** - Customer name (Text)
6. **description** - Sale description (Text)
7. **platform** - Platform name (e.g., "facebook", "instagram")
8. **id_order_m** - Order mother ID (can be empty string)
9. **price** - Product price (Decimal)
10. **quantity** - Product quantity (Integer)
11. **product_name** - Product name (Text)
12. **variant_name** - Variant name (Text)
13. **sku_product** - Product SKU (Text)
14. **sku_variant** - Variant SKU (Text)

### Example CSV
```csv
id_sale,amount,effectued_at,from_number,from_name,description,platform,id_order_m,price,quantity,product_name,variant_name,sku_product,sku_variant
1,160.89,2025-11-06 16:22,261974652510,"Jane Smith","Sale of products",instagram,,16.07,3,"Product F","Variant 2",PT,PDT3
1,160.89,2025-11-06 16:22,261974652510,"Jane Smith","Sale of products",instagram,,28.17,4,"Product B","Deluxe",PWD,SMCH-BLACK
```

### Important Notes
- Multiple rows with the same `id_sale` represent multiple products in a single sale
- Fields with commas or special characters should be quoted
- Empty fields should be left blank (not null)
- Date format must be exactly `yyyy-MM-dd HH:mm`

## API Endpoint

### Import Sales from CSV
**Endpoint:** `POST /api/sales/import`

**Headers:**
- `Authorization: Bearer <token>`
- `Content-Type: multipart/form-data`

**Parameters:**
- `file`: CSV file (multipart file upload)

**Request Example (cURL):**
```bash
curl -X POST "http://localhost:8080/api/sales/import" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@sales.csv"
```

**Success Response (200):**
```json
{
  "status": 200,
  "data": {
    "totalRows": 20,
    "successfulImports": 20,
    "failedImports": 0,
    "salesCreated": 10,
    "salesDetailsCreated": 20,
    "errors": [],
    "warnings": []
  },
  "errors": null
}
```

**Error Response (400):**
```json
{
  "status": 400,
  "data": {
    "totalRows": 20,
    "successfulImports": 15,
    "failedImports": 5,
    "salesCreated": 8,
    "salesDetailsCreated": 15,
    "errors": [
      "Error at line 5: Invalid date format",
      "Error processing sale ID 3: Failed to create customer"
    ],
    "warnings": [
      "Platform 'twitter' not found, using default",
      "Product not found for SKU: UNKNOWN"
    ]
  },
  "errors": ["Import completed with errors"]
}
```

## How It Works

### 1. File Validation
- Checks if file is uploaded
- Validates CSV file type
- Authenticates the seller

### 2. CSV Parsing
- Reads and parses each line
- Validates required fields
- Groups rows by sale ID

### 3. Data Processing
For each sale:
- **Customer Creation**: Finds or creates a potential customer based on phone number
- **Sale Creation**: Creates a sale record with the provided information
- **Details Creation**: Creates sales detail records for each product in the sale

### 4. Product/Variant Matching
- Attempts to match products by SKU prefix
- Attempts to match variants by SKU
- Uses default IDs (1) if products/variants are not found
- Logs warnings for unmatched SKUs

### 5. Platform Handling
- Looks up platform ID from `supported_platforms_v2` table
- Falls back to Facebook (ID: 1) if platform not found

## Database Impact

### Tables Modified
1. **sales** - Creates new sale records
2. **sales_details_v2** - Creates detail records for each product
3. **potential_customers_v2** - Creates new customers if they don't exist

### Default Values
- `id_spn`: 1 (default phone number ID)
- `status`: 1 (default status)
- `paid_amount`: 0.0
- `id_product`: 1 (if SKU not found)
- `id_variant`: 1 (if SKU not found)
- `id_order_m`: 0 (if empty in CSV)

## Error Handling

### Common Errors
1. **Authentication Error (401)**
   - Cause: Invalid or missing authorization token
   - Solution: Ensure valid token is provided

2. **Invalid File (400)**
   - Cause: File is not CSV or is empty
   - Solution: Upload a valid CSV file

3. **Parse Error (400)**
   - Cause: Invalid CSV format, wrong number of columns, or invalid data types
   - Solution: Check CSV format matches specification

4. **Processing Error (partial success)**
   - Cause: Some rows failed to process
   - Solution: Check response for specific error details

### Warnings vs Errors
- **Warnings**: Non-critical issues (e.g., SKU not found, using defaults)
- **Errors**: Critical failures (e.g., invalid data, database issues)

## Implementation Details

### Key Components

1. **SalesCsvRow.java** - DTO for CSV row data
2. **ImportResult.java** - Response DTO with statistics
3. **SalesCsvImportService.java** - Main service handling import logic
4. **SalesController.java** - REST endpoint for file upload

### Service Methods

```java
// Main import method
public ImportResult importFromCsv(MultipartFile file, Long sellerId)

// CSV parsing
private List<SalesCsvRow> parseCsvFile(MultipartFile file)
private SalesCsvRow parseCsvLine(String line)

// Data processing
private void processSale(List<SalesCsvRow> rows, Long sellerId, ImportResult result)
private PotentialCustomerV2 findOrCreateCustomer(SalesCsvRow row, Long sellerId, ImportResult result)
private SalesDetails createSalesDetail(SalesCsvRow row, Sales sale, Long sellerId)

// Product/Variant lookup
private Long findProductIdBySku(String sku, Long sellerId)
private Long findVariantIdBySku(String sku, Long sellerId)
```

### Transaction Handling
- Each sale import is wrapped in a transaction
- If a sale fails, it doesn't affect other sales
- Partial imports are supported

## Testing

### Test File Location
- Sample CSV: `/ddl/sales.csv`

### Testing Steps
1. Get authentication token for a seller
2. Upload the CSV file using the API endpoint
3. Verify response shows correct statistics
4. Check database for created records

### Verification Queries
```sql
-- Check created sales
SELECT * FROM sales WHERE id_seller = YOUR_SELLER_ID ORDER BY id_sale DESC;

-- Check sales details
SELECT * FROM sales_details_v2 WHERE id_sale_m IN 
  (SELECT id_sale FROM sales WHERE id_seller = YOUR_SELLER_ID);

-- Check created customers
SELECT * FROM potential_customers_v2 WHERE identifier_on_platform IN 
  (SELECT from_number FROM sales WHERE id_seller = YOUR_SELLER_ID);
```

## Future Enhancements

1. **Async Processing**: For large CSV files, implement background processing
2. **Duplicate Detection**: Add logic to prevent duplicate imports
3. **Validation Rules**: More sophisticated validation for business rules
4. **Export Feature**: Allow exporting sales data to CSV format
5. **SKU Matching Improvement**: Better algorithms for fuzzy SKU matching
6. **Batch Import**: Support for multiple CSV files at once

## Troubleshooting

### Issue: All products/variants show ID 1
**Cause**: SKU matching is not finding products
**Solution**: 
- Ensure products have `sku_prefix` set in database
- Ensure variants have `sku` set in database
- Check CSV SKU values match database values

### Issue: Platform not found warning
**Cause**: Platform name in CSV doesn't match database
**Solution**: Use lowercase platform names: "facebook", "instagram", "x", "thread"

### Issue: Customer creation fails
**Cause**: Missing required fields or database constraint violation
**Solution**: Check that customer name and phone number are valid

### Issue: Date parsing errors
**Cause**: Date format doesn't match expected format
**Solution**: Ensure dates are in format `yyyy-MM-dd HH:mm` (e.g., "2025-11-06 16:22")

## Support
For issues or questions, please contact the development team or create an issue in the project repository.

