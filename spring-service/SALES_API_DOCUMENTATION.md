# Sales API Endpoint Documentation

## Overview
This document describes the new sales endpoint that allows sellers to view their sales with pagination support, similar to the orders endpoint.

## Endpoint

### GET /api/sales

Retrieves all sales for the authenticated seller with pagination support.

#### Request

**Headers:**
- `Authorization`: Bearer token for the authenticated seller (required)

**Query Parameters:**
- `page`: Page number (0-based, optional, default: 0)
- `size`: Number of items per page (optional, default: 20)
- `sort`: Sort criteria (optional, e.g., `effectuatedAt,desc`)

#### Response

**Success Response (200 OK):**

```json
{
  "status": 200,
  "data": {
    "sales": [
      {
        "idSale": 1,
        "amount": 150.50,
        "effectuatedAt": "2025-11-01T10:30:00",
        "fromNumber": "+261340000000",
        "fromName": "John Doe",
        "description": "Payment for order #123",
        "idSpn": 1,
        "idOrderM": 123,
        "idPc": "PC001",
        "details": [...]
      }
    ],
    "totalSales": 150
  },
  "errors": null
}
```

**Error Responses:**

- **401 Unauthorized:** When the seller is not authenticated
```json
{
  "status": 401,
  "data": null,
  "errors": ["Please log in to view sales"]
}
```

- **500 Internal Server Error:** When an unexpected error occurs
```json
{
  "status": 500,
  "data": null,
  "errors": [...]
}
```

## Implementation Details

### Created Files

1. **SalesToDisplay.java** - DTO for wrapping sales list with total count
   - Location: `src/main/java/com/itu/socialcom/demo/sales/SalesToDisplay.java`
   - Fields:
     - `List<Sales> sales`: List of sales for the current page
     - `int totalSales`: Total count of all sales for the seller

2. **SalesController.java** - REST controller for sales endpoints
   - Location: `src/main/java/com/itu/socialcom/demo/sales/SalesController.java`
   - Endpoints:
     - `GET /api/sales`: Fetch all sales for authenticated seller with pagination

### Modified Files

1. **SalesRepository.java** - Added custom queries
   - `findAllByIdSeller(Integer idSeller, Pageable pageable)`: Fetch sales by seller ID with pagination
   - `countByIdSeller(Integer idSeller)`: Count total sales for a seller
   - Both methods use JPQL joins to connect Sales → OrderParent → Seller

## Database Relationships

Sales are linked to sellers through the following relationship:
```
Sales → (id_order_m) → OrderParent → (id_seller) → Seller
```

The JPQL queries join these tables to filter sales by seller ID.

## Usage Example

```bash
# Fetch first page of sales (20 items)
curl -X GET "http://localhost:8080/api/sales" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Fetch second page with 10 items per page
curl -X GET "http://localhost:8080/api/sales?page=1&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Fetch sales sorted by amount in descending order
curl -X GET "http://localhost:8080/api/sales?sort=amount,desc" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Notes

- The endpoint follows the same authentication and response structure as `/api/orders`
- Sales are ordered by `effectuatedAt` (date) in descending order by default
- The `totalSales` field in the response provides the total count across all pages
- Only sales associated with orders belonging to the authenticated seller are returned

