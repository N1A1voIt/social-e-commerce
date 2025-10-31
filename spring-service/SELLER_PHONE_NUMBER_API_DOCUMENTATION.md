# Seller Phone Number Configuration API Documentation

## Overview
This API allows sellers to configure their phone numbers for different payment methods (e.g., MVola, Orange Money). Each seller can associate a phone number with a specific payment method, making it easier to manage payment collection across different platforms.

## Base URL
```
http://localhost:8080/api
```

## Authentication
All endpoints require Bearer token authentication in the Authorization header:
```
Authorization: Bearer <your-firebase-token>
```

---

## Endpoints

### 1. Get All Available Payment Methods

Get a list of all payment methods available in the system.

**Endpoint:** `GET /payment-methods`

**Authentication:** Not required (public endpoint)

**Response:**
```json
{
  "status": 200,
  "data": [
    {
      "id": 1,
      "paymentName": "MVola"
    },
    {
      "id": 2,
      "paymentName": "Orange Money"
    }
  ],
  "errors": null
}
```

**Example Request:**
```javascript
// Using fetch
const response = await fetch('http://localhost:8080/api/payment-methods', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
});
const data = await response.json();
```

---

### 2. Get Payment Method by ID

Get details of a specific payment method.

**Endpoint:** `GET /payment-methods/{id}`

**Authentication:** Not required (public endpoint)

**Path Parameters:**
- `id` (Long) - Payment method ID

**Response:**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "paymentName": "MVola"
  },
  "errors": null
}
```

---

### 3. Get Payment Method by Name

Get details of a payment method by its name.

**Endpoint:** `GET /payment-methods/name/{name}`

**Authentication:** Not required (public endpoint)

**Path Parameters:**
- `name` (String) - Payment method name (e.g., "MVola", "Orange Money")

---

### 4. Create or Update Phone Number Configuration

Configure or update a phone number for a specific payment method. If a configuration already exists for the payment method, it will be updated.

**Endpoint:** `POST /sellers/phone-numbers`

**Authentication:** Required (Bearer token)

**Request Body:**
```json
{
  "phoneNumber": "+261340000000",
  "associatedName": "John Doe",
  "paymentMethodId": 1
}
```

**Field Descriptions:**
- `phoneNumber` (String, required) - The phone number to associate with the payment method
- `associatedName` (String, required) - Name associated with the phone number (account holder name)
- `paymentMethodId` (Long, required) - ID of the payment method

**Response:**
```json
{
  "status": 201,
  "data": {
    "id": 1,
    "phoneNumber": "+261340000000",
    "associatedName": "John Doe",
    "paymentMethodId": 1,
    "paymentMethodName": "MVola",
    "sellerId": 123
  },
  "errors": null
}
```

**Example Request:**
```javascript
// Using fetch with authentication
const response = await fetch('http://localhost:8080/api/sellers/phone-numbers', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${firebaseToken}`
  },
  body: JSON.stringify({
    phoneNumber: '+261340000000',
    associatedName: 'John Doe',
    paymentMethodId: 1
  })
});
const data = await response.json();
```

**Error Response (400 - Bad Request):**
```json
{
  "status": 400,
  "data": null,
  "errors": [
    {
      "message": "Payment method not found with id: 1"
    }
  ]
}
```

---

### 5. Get All Phone Numbers for Current Seller

Retrieve all phone number configurations for the authenticated seller.

**Endpoint:** `GET /sellers/phone-numbers`

**Authentication:** Required (Bearer token)

**Response:**
```json
{
  "status": 200,
  "data": [
    {
      "id": 1,
      "phoneNumber": "+261340000000",
      "associatedName": "John Doe",
      "paymentMethodId": 1,
      "paymentMethodName": "MVola",
      "sellerId": 123
    },
    {
      "id": 2,
      "phoneNumber": "+261320000000",
      "associatedName": "John Doe Business",
      "paymentMethodId": 2,
      "paymentMethodName": "Orange Money",
      "sellerId": 123
    }
  ],
  "errors": null
}
```

**Example Request:**
```javascript
const response = await fetch('http://localhost:8080/api/sellers/phone-numbers', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${firebaseToken}`
  }
});
const data = await response.json();
```

---

### 6. Get Phone Number Configuration by ID

Retrieve a specific phone number configuration.

**Endpoint:** `GET /sellers/phone-numbers/{id}`

**Authentication:** Required (Bearer token)

**Path Parameters:**
- `id` (Long) - Phone number configuration ID

**Response:**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "phoneNumber": "+261340000000",
    "associatedName": "John Doe",
    "paymentMethodId": 1,
    "paymentMethodName": "MVola",
    "sellerId": 123
  },
  "errors": null
}
```

**Error Response (403 - Forbidden):**
```json
{
  "status": 403,
  "data": null,
  "errors": null
}
```
*This error occurs when trying to access a phone number configuration that doesn't belong to the authenticated seller.*

---

### 7. Get Phone Number for Specific Payment Method

Retrieve the phone number configuration for a specific payment method.

**Endpoint:** `GET /sellers/phone-numbers/payment-method/{paymentMethodId}`

**Authentication:** Required (Bearer token)

**Path Parameters:**
- `paymentMethodId` (Long) - Payment method ID

**Response:**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "phoneNumber": "+261340000000",
    "associatedName": "John Doe",
    "paymentMethodId": 1,
    "paymentMethodName": "MVola",
    "sellerId": 123
  },
  "errors": null
}
```

**Error Response (404 - Not Found):**
```json
{
  "status": 404,
  "data": null,
  "errors": [
    {
      "message": "Phone number configuration not found for seller 123 and payment method 1"
    }
  ]
}
```

---

### 8. Update Phone Number Configuration

Update an existing phone number configuration.

**Endpoint:** `PUT /sellers/phone-numbers/{id}`

**Authentication:** Required (Bearer token)

**Path Parameters:**
- `id` (Long) - Phone number configuration ID

**Request Body:**
```json
{
  "phoneNumber": "+261340000001",
  "associatedName": "John Doe Updated",
  "paymentMethodId": 1
}
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "phoneNumber": "+261340000001",
    "associatedName": "John Doe Updated",
    "paymentMethodId": 1,
    "paymentMethodName": "MVola",
    "sellerId": 123
  },
  "errors": null
}
```

---

### 9. Delete Phone Number Configuration

Delete a phone number configuration.

**Endpoint:** `DELETE /sellers/phone-numbers/{id}`

**Authentication:** Required (Bearer token)

**Path Parameters:**
- `id` (Long) - Phone number configuration ID

**Response:**
```json
{
  "status": 200,
  "data": null,
  "errors": null
}
```

**Error Response (400 - Bad Request):**
```json
{
  "status": 400,
  "data": null,
  "errors": [
    {
      "message": "Unauthorized: Phone number configuration does not belong to this seller"
    }
  ]
}
```

---

## Frontend Implementation Example

### React/Next.js Example

```javascript
// services/phoneNumberService.js
import { auth } from './firebase';

const API_BASE_URL = 'http://localhost:8080/api';

export const phoneNumberService = {
  // Get all payment methods
  async getPaymentMethods() {
    const response = await fetch(`${API_BASE_URL}/payment-methods`);
    return response.json();
  },

  // Get seller's phone numbers
  async getSellerPhoneNumbers() {
    const token = await auth.currentUser?.getIdToken();
    const response = await fetch(`${API_BASE_URL}/sellers/phone-numbers`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.json();
  },

  // Create or update phone number
  async savePhoneNumber(data) {
    const token = await auth.currentUser?.getIdToken();
    const response = await fetch(`${API_BASE_URL}/sellers/phone-numbers`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(data)
    });
    return response.json();
  },

  // Delete phone number
  async deletePhoneNumber(id) {
    const token = await auth.currentUser?.getIdToken();
    const response = await fetch(`${API_BASE_URL}/sellers/phone-numbers/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.json();
  }
};
```

### React Component Example

```jsx
// components/PhoneNumberConfig.jsx
import React, { useState, useEffect } from 'react';
import { phoneNumberService } from '../services/phoneNumberService';

function PhoneNumberConfig() {
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [phoneNumbers, setPhoneNumbers] = useState([]);
  const [formData, setFormData] = useState({
    phoneNumber: '',
    associatedName: '',
    paymentMethodId: ''
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [methodsRes, numbersRes] = await Promise.all([
        phoneNumberService.getPaymentMethods(),
        phoneNumberService.getSellerPhoneNumbers()
      ]);
      
      if (methodsRes.status === 200) {
        setPaymentMethods(methodsRes.data);
      }
      if (numbersRes.status === 200) {
        setPhoneNumbers(numbersRes.data);
      }
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const response = await phoneNumberService.savePhoneNumber({
        ...formData,
        paymentMethodId: parseInt(formData.paymentMethodId)
      });
      
      if (response.status === 201 || response.status === 200) {
        alert('Phone number saved successfully!');
        loadData(); // Reload data
        setFormData({ phoneNumber: '', associatedName: '', paymentMethodId: '' });
      } else {
        alert('Error saving phone number');
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error saving phone number');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this phone number?')) return;
    
    try {
      setLoading(true);
      const response = await phoneNumberService.deletePhoneNumber(id);
      if (response.status === 200) {
        alert('Phone number deleted successfully!');
        loadData();
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error deleting phone number');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="phone-number-config">
      <h2>Payment Phone Numbers Configuration</h2>
      
      {/* Form to add/update phone number */}
      <form onSubmit={handleSubmit}>
        <div>
          <label>Payment Method:</label>
          <select
            value={formData.paymentMethodId}
            onChange={(e) => setFormData({ ...formData, paymentMethodId: e.target.value })}
            required
          >
            <option value="">Select a payment method</option>
            {paymentMethods.map(method => (
              <option key={method.id} value={method.id}>
                {method.paymentName}
              </option>
            ))}
          </select>
        </div>
        
        <div>
          <label>Phone Number:</label>
          <input
            type="tel"
            value={formData.phoneNumber}
            onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
            placeholder="+261340000000"
            required
          />
        </div>
        
        <div>
          <label>Account Holder Name:</label>
          <input
            type="text"
            value={formData.associatedName}
            onChange={(e) => setFormData({ ...formData, associatedName: e.target.value })}
            placeholder="John Doe"
            required
          />
        </div>
        
        <button type="submit" disabled={loading}>
          {loading ? 'Saving...' : 'Save Phone Number'}
        </button>
      </form>

      {/* List of configured phone numbers */}
      <div className="phone-numbers-list">
        <h3>Configured Phone Numbers</h3>
        {phoneNumbers.length === 0 ? (
          <p>No phone numbers configured yet.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Payment Method</th>
                <th>Phone Number</th>
                <th>Account Holder</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {phoneNumbers.map(number => (
                <tr key={number.id}>
                  <td>{number.paymentMethodName}</td>
                  <td>{number.phoneNumber}</td>
                  <td>{number.associatedName}</td>
                  <td>
                    <button onClick={() => handleDelete(number.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default PhoneNumberConfig;
```

---

## Data Models

### SellerPhoneNumberRequest
```typescript
interface SellerPhoneNumberRequest {
  phoneNumber: string;      // Required, max 50 characters
  associatedName: string;   // Required
  paymentMethodId: number;  // Required
}
```

### SellerPhoneNumberResponse
```typescript
interface SellerPhoneNumberResponse {
  id: number;
  phoneNumber: string;
  associatedName: string;
  paymentMethodId: number;
  paymentMethodName: string;
  sellerId: number;
}
```

### PaymentMethodResponse
```typescript
interface PaymentMethodResponse {
  id: number;
  paymentName: string;
}
```

### ApiResponse
```typescript
interface ApiResponse<T = any> {
  status: number;
  data: T | null;
  errors: Array<{ message: string }> | null;
}
```

---

## Business Rules

1. **One Phone Number Per Payment Method**: Each seller can only configure one phone number per payment method. If you try to add a second phone number for the same payment method, it will update the existing one.

2. **Authentication Required**: All seller phone number endpoints require authentication. The seller can only view, create, update, or delete their own phone numbers.

3. **Payment Method Must Exist**: Before configuring a phone number, the payment method must exist in the database.

4. **Phone Number Format**: While the API doesn't enforce a specific format, it's recommended to use international format (e.g., +261340000000).

---

## Error Handling

All endpoints follow a consistent error response format:

```json
{
  "status": <HTTP_STATUS_CODE>,
  "data": null,
  "errors": [
    {
      "message": "Error description"
    }
  ]
}
```

Common HTTP status codes:
- `200` - Success
- `201` - Created
- `400` - Bad Request (validation error, missing data)
- `401` - Unauthorized (missing or invalid token)
- `403` - Forbidden (trying to access someone else's data)
- `404` - Not Found
- `500` - Internal Server Error

---

## Testing with Postman/cURL

### Example cURL Commands

**Get all payment methods:**
```bash
curl -X GET http://localhost:8080/api/payment-methods
```

**Create phone number configuration:**
```bash
curl -X POST http://localhost:8080/api/sellers/phone-numbers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN" \
  -d '{
    "phoneNumber": "+261340000000",
    "associatedName": "John Doe",
    "paymentMethodId": 1
  }'
```

**Get all seller phone numbers:**
```bash
curl -X GET http://localhost:8080/api/sellers/phone-numbers \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

**Delete phone number:**
```bash
curl -X DELETE http://localhost:8080/api/sellers/phone-numbers/1 \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

---

## Database Schema

### Tables

**payment_method_v2**
```sql
CREATE TABLE payment_method_v2 (
    id_pm SERIAL PRIMARY KEY,
    payment_name TEXT UNIQUE NOT NULL
);
```

**sellers_phone_number_e**
```sql
CREATE TABLE sellers_phone_number_e (
    id_spn SERIAL PRIMARY KEY,
    phone_number VARCHAR(50) NOT NULL,
    associated_name TEXT NOT NULL,
    id_pm INTEGER NOT NULL REFERENCES payment_method_v2(id_pm),
    id_seller INTEGER NOT NULL REFERENCES seller_v2(id_seller)
);
```

---

## Notes for Frontend Developers

1. **Token Management**: Make sure to handle token refresh properly. Firebase tokens expire after 1 hour.

2. **Loading States**: Always show loading indicators during API calls to improve UX.

3. **Error Handling**: Display user-friendly error messages. Check the `errors` array in the response.

4. **Form Validation**: Validate phone numbers on the frontend before sending to the API.

5. **Caching**: Consider caching payment methods as they rarely change.

6. **Real-time Updates**: If using real-time features, refresh the phone numbers list after create/update/delete operations.

7. **Mobile Responsiveness**: Ensure forms work well on mobile devices, especially for phone number input.

8. **Accessibility**: Add proper labels and ARIA attributes for form inputs.

---

## Support

For any questions or issues, please contact the backend development team.

