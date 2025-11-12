# Order Filters Implementation

## Overview
This document describes the implementation of filtering functionality for the orders table with backend support. Users can now filter orders by status, customer name, and date range.

## Implementation Date
November 12, 2025

## Features
- **Status Filter**: Filter orders by their current status (Pending Payment, Paid, In Delivery, Completed, Cancelled, etc.)
- **Customer Name Search**: Search orders by customer name (case-insensitive, partial match)
- **Date Range Filter**: Filter orders by creation date with start and end date pickers
- **Backend Processing**: All filters are processed on the backend for optimal performance
- **Clear Filters**: Easy way to reset all filters and view all orders

## Backend Changes

### 1. OrderParentRepository
**File:** `spring-service/src/main/java/com/itu/socialcom/demo/orders/repository/OrderParentRepository.java`

Added two new query methods:
- `findAllByIdSellerWithFilters()`: Returns paginated results with applied filters
- `countByIdSellerWithFilters()`: Returns total count for pagination with filters

Both methods support optional filters:
- `status`: Filter by order status (exact match)
- `customerName`: Search by customer name (case-insensitive, partial match)
- `startDate`: Filter orders created on or after this date
- `endDate`: Filter orders created on or before this date

### 2. OrderController
**File:** `spring-service/src/main/java/com/itu/socialcom/demo/orders/controller/OrderController.java`

Updated `GET /api/orders` endpoint to accept optional query parameters:
- `status` (Integer, optional): Order status code
- `customerName` (String, optional): Customer name to search
- `startDate` (String, optional): ISO 8601 formatted date-time string
- `endDate` (String, optional): ISO 8601 formatted date-time string

The endpoint now:
1. Parses date parameters from ISO 8601 format
2. Uses filtered query when any filter is provided
3. Falls back to unfiltered query when no filters are applied
4. Returns paginated results with total count

## Frontend Changes

### 3. OrderService
**File:** `social-front-end/src/app/main/orders/order.service.ts`

Updated `fetchAllOrders()` method to accept filter parameters:
```typescript
fetchAllOrders(
  pageNum: number, 
  status?: number | null, 
  customerName?: string | null, 
  startDate?: string | null, 
  endDate?: string | null
): Observable<ApiResponse>
```

The service:
- Builds URL with query parameters based on provided filters
- Properly encodes filter values for URL transmission
- Only adds non-null/non-empty parameters to the request

### 4. OrdersComponent TypeScript
**File:** `social-front-end/src/app/main/orders/orders.component.ts`

Added:
- **Filter State Properties**:
  - `filterStatus`: Selected status filter
  - `filterCustomerName`: Customer name search text
  - `filterStartDate`: Start date for date range filter
  - `filterEndDate`: End date for date range filter
  - `statusOptions`: Array of status options for dropdown

- **Filter Methods**:
  - `applyFilters()`: Applies current filter values and refreshes data
  - `clearFilters()`: Resets all filters and refreshes data
  - `formatDateToISO()`: Converts Date objects to ISO 8601 format for backend

- **Updated Dependencies**: Added PrimeNG modules:
  - `CalendarModule`: For date pickers
  - `InputTextModule`: For text input
  - `DropdownModule`: For status dropdown

### 5. OrdersComponent Template
**File:** `social-front-end/src/app/main/orders/orders.component.html`

Added filter section above the orders table with:
- **Status Dropdown**: Allows selection from predefined status options
- **Customer Name Input**: Text field for searching customer names
- **Start Date Calendar**: Date picker for start date
- **End Date Calendar**: Date picker for end date
- **Action Buttons**:
  - "Apply Filters": Triggers filtering with current values
  - "Clear Filters": Resets all filters

## Status Codes
The following status codes are available for filtering:
- `1`: Pending Payment
- `11`: Paid
- `21`: In Delivery
- `25`: Delivery Available
- `5`: Completed
- `31`: Cancelled

## Usage

### Frontend
1. Navigate to the Orders page
2. Use the filter panel at the top of the page:
   - Select a status from the dropdown
   - Enter customer name to search
   - Pick start and/or end dates
3. Click "Apply Filters" to filter results
4. Click "Clear Filters" to reset and view all orders

### Backend API
```bash
# Example: Get orders with status 11 (Paid)
GET /api/orders?status=11

# Example: Search orders by customer name
GET /api/orders?customerName=John

# Example: Get orders within date range
GET /api/orders?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59

# Example: Combined filters
GET /api/orders?status=11&customerName=John&startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59&page=0&size=10
```

## Technical Details

### Date Handling
- Frontend sends dates in ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`
- Backend parses dates using `LocalDateTime.parse()`
- All date comparisons use `>=` for start date and `<=` for end date

### Performance Considerations
- Filters are applied at the database level using JPQL queries
- Pagination is maintained across filtered results
- Indexes on `id_seller`, `d_status`, and `created_at` columns are recommended

### Null Handling
- All filter parameters are optional
- Null or empty values are ignored in the query
- The dropdown includes "All Statuses" option that passes null

## Testing Recommendations
1. Test each filter independently
2. Test combined filters
3. Test with empty/null values
4. Test date range edge cases (same start/end date, end before start)
5. Test pagination with filtered results
6. Test with special characters in customer names

## Future Enhancements
Potential improvements:
- Add filter by total amount range
- Add filter by shipping address
- Add saved filter presets
- Add export filtered results
- Add filter by order ID
