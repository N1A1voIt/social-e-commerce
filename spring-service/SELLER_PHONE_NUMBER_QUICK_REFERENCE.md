# Quick Reference: Seller Phone Number Configuration

## File Structure
```
src/main/java/com/itu/socialcom/demo/
├── moneytransactions/
│   ├── PaymentMethod.java                    (Entity)
│   ├── PaymentMethodRepository.java          (Repository)
│   ├── PaymentMethodService.java             (Service Interface)
│   ├── PaymentMethodServiceImpl.java         (Service Implementation)
│   └── controller/
│       └── PaymentMethodController.java      (REST Controller)
│
└── authentication/user/phonenumber/
    ├── SellerPhoneNumber.java                (Entity)
    ├── SellerPhoneNumberRepository.java      (Repository)
    ├── SellerPhoneNumberService.java         (Service Interface)
    ├── SellerPhoneNumberServiceImpl.java     (Service Implementation)
    ├── controller/
    │   └── SellerPhoneNumberController.java  (REST Controller)
    └── dto/
        ├── SellerPhoneNumberRequest.java     (Request DTO)
        ├── SellerPhoneNumberResponse.java    (Response DTO)
        └── PaymentMethodResponse.java        (Response DTO)
```

## Quick API Reference

### Get Payment Methods (No Auth)
```bash
GET /api/payment-methods
```

### Configure Phone Number (Auth Required)
```bash
POST /api/sellers/phone-numbers
Content-Type: application/json
Authorization: Bearer <token>

{
  "phoneNumber": "+261340000000",
  "associatedName": "John Doe",
  "paymentMethodId": 1
}
```

### Get All Seller Phone Numbers (Auth Required)
```bash
GET /api/sellers/phone-numbers
Authorization: Bearer <token>
```

### Delete Phone Number (Auth Required)
```bash
DELETE /api/sellers/phone-numbers/{id}
Authorization: Bearer <token>
```

## Database Tables

### payment_method_v2
```sql
id_pm (PK) | payment_name
-----------|--------------
1          | MVola
2          | Orange Money
```

### sellers_phone_number_e
```sql
id_spn (PK) | phone_number    | associated_name | id_pm (FK) | id_seller (FK)
------------|-----------------|-----------------|------------|----------------
1           | +261340000000   | John Doe        | 1          | 123
```

## Key Classes

### SellerPhoneNumberRequest
```java
{
  String phoneNumber;      // Required
  String associatedName;   // Required
  Long paymentMethodId;    // Required
}
```

### SellerPhoneNumberResponse
```java
{
  Long id;
  String phoneNumber;
  String associatedName;
  Long paymentMethodId;
  String paymentMethodName;
  Long sellerId;
}
```

## Frontend React Hook Example
```javascript
const usePhoneNumbers = () => {
  const [phoneNumbers, setPhoneNumbers] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadPhoneNumbers = async () => {
    setLoading(true);
    const token = await auth.currentUser?.getIdToken();
    const res = await fetch('/api/sellers/phone-numbers', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const data = await res.json();
    setPhoneNumbers(data.data || []);
    setLoading(false);
  };

  const savePhoneNumber = async (formData) => {
    const token = await auth.currentUser?.getIdToken();
    const res = await fetch('/api/sellers/phone-numbers', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(formData)
    });
    return res.json();
  };

  return { phoneNumbers, loading, loadPhoneNumbers, savePhoneNumber };
};
```

## Error Codes
- **200** - Success
- **201** - Created
- **400** - Bad Request (validation error)
- **401** - Unauthorized (missing/invalid token)
- **403** - Forbidden (not your resource)
- **404** - Not Found
- **500** - Internal Server Error

## Important Notes
1. ✅ One phone number per payment method per seller
2. ✅ Creating duplicate updates existing record
3. ✅ All seller endpoints require authentication
4. ✅ Payment methods endpoint is public
5. ✅ Authorization check prevents accessing other sellers' data

## Documentation Files
- **SELLER_PHONE_NUMBER_API_DOCUMENTATION.md** - Complete API docs for frontend
- **SELLER_PHONE_NUMBER_IMPLEMENTATION_SUMMARY.md** - Technical implementation details
- **SELLER_PHONE_NUMBER_QUICK_REFERENCE.md** - This file

