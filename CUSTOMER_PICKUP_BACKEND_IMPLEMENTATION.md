# Customer Pickup Backend Implementation

## Overview
This implementation adds support for customer pickup orders in the e-commerce system. When a seller chooses "Customer Pickup" for an order, the system follows a specific flow that differs from the delivery flow.

## Status Flow
```
Status 1 (Created) 
    → Status 26 (Waiting for customer) [via /api/order/customer-pickup]
        → Status 51 (Completed) [via /api/order/complete-pickup]
```

## New Files Created

### 1. CustomerPickupService.java
**Location:** `spring-service/src/main/java/com/itu/socialcom/demo/orders/service/CustomerPickupService.java`

**Purpose:** Service layer handling the business logic for customer pickup orders.

**Key Methods:**

#### `setCustomerPickup(Long orderId)`
- **Input:** Order ID
- **Actions:**
  1. Validates order exists and is in status 1 (Created)
  2. Changes order status to 26 (Waiting for customer)
  3. Creates a Sale record with:
     - Amount = order total
     - Paid amount = 0
     - Status = 1 (Unpaid/Pending payment)
  4. Creates SalesDetails for each order item
  5. Moves stock (creates stock movement records)
- **Returns:** Updated OrderParent
- **Exception:** If order not found or invalid status

#### `completeCustomerPickup(Long orderId)`
- **Input:** Order ID
- **Actions:**
  1. Validates order exists and is in status 26 (Waiting for customer)
  2. Finds the associated Sale record
  3. Creates a Payment record:
     - Amount = full sale amount
     - Payment method = "Cash" (ID: 2)
     - Links to the sale
  4. Updates Sale:
     - Paid amount = full amount
     - Status = 11 (Fully paid)
  5. Changes order status to 51 (Completed)
- **Returns:** Updated OrderParent
- **Exception:** If order or sale not found, or invalid status

## Modified Files

### 2. OrderController.java
**Location:** `spring-service/src/main/java/com/itu/socialcom/demo/orders/controller/OrderController.java`

**Changes:**
- Added `@Autowired CustomerPickupService customerPickupService`
- Added two new endpoints:

#### POST `/api/order/customer-pickup`
- **Purpose:** Set order to customer pickup mode
- **Request Body:**
  ```json
  {
    "orderId": 123
  }
  ```
- **Response:** Updated order with status 26
- **Authentication:** Required (Bearer token)

#### POST `/api/order/complete-pickup`
- **Purpose:** Complete customer pickup order
- **Request Body:**
  ```json
  {
    "orderId": 123
  }
  ```
- **Response:** Updated order with status 51
- **Authentication:** Required (Bearer token)

## Database Changes

### Sales Table
New records created with:
- `paid_amount = 0` when pickup is initiated
- `paid_amount = amount` when pickup is completed
- `status = 1` (Unpaid) → `status = 11` (Fully paid)

### Payments Table
New record created on completion:
- `amount = full_order_amount`
- `payment_method = "Cash"`
- `id_pm = 2` (Cash payment method ID)
- `id_sales = corresponding_sale_id`

### Stock_Parent & Stock_Child Tables
Stock movement records created with:
- Description: "Move of order {description} (Customer Pickup)"
- Output quantities for each product

## Integration with Frontend

The frontend service calls these endpoints:
1. When user selects "Customer Pickup" → calls `/api/order/customer-pickup`
2. When seller clicks "Next Step" on status 26 order → calls `/api/order/complete-pickup`

## Error Handling

All endpoints include comprehensive error handling:
- **401:** User not authenticated
- **404:** Order or Sale not found
- **500:** Invalid status transitions or other processing errors

## Testing Recommendations

1. **Test Status Transition Flow:**
   - Create order (status 1)
   - Set customer pickup (status 1 → 26)
   - Complete pickup (status 26 → 51)

2. **Test Sale Creation:**
   - Verify sale is created with paid_amount = 0
   - Verify sale details match order items

3. **Test Payment Creation:**
   - Verify payment record created on completion
   - Verify payment method is "Cash"
   - Verify sale paid_amount updated to full amount

4. **Test Stock Movement:**
   - Verify stock records created correctly
   - Verify quantities match order items

5. **Test Error Scenarios:**
   - Invalid order ID
   - Wrong status transitions
   - Missing authentication

## Status Codes Reference

- **1:** Created
- **26:** Waiting for customer (NEW)
- **51:** Completed

## Payment Method IDs

- **1:** MVola
- **2:** Cash

## Sale Status Codes

- **1:** Unpaid/Pending payment
- **11:** Fully paid
