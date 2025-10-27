# ShippingPointController Update - managedPageId=-1 Enhancement

## Changes Made

### Updated Method: `getShippingPointsByManagedPage`

**Endpoint:** `GET /api/shipping-points/managed-page/{managedPageId}`

### New Behavior

1. **When `managedPageId == -1`:**
   - Returns ALL shipping points linked to the authenticated user (seller)
   - Uses `shippingPointService.getShippingPointsBySellerId(seller.getId())`

2. **When `managedPageId` is a valid ID:**
   - Returns shipping points for that specific managed page
   - Verifies the managed page belongs to the seller (security check)
   - Returns 403 Forbidden if the managed page doesn't belong to the seller

3. **Place Name Formatting:**
   - For ALL returned shipping points, the `place_name` is formatted as: `{origin} -> {place_name}`
   - Only applies if `origin` is not null and not empty
   - Example: `"Antananarivo -> Central Warehouse"` instead of just `"Central Warehouse"`

### Code Changes

```java
// Special case: if managedPageId is -1, return all shipping points for the seller
if (managedPageId == -1) {
    shippingPoints = shippingPointService.getShippingPointsBySellerId(seller.getId());
} else {
    // Existing logic for specific managed page
    shippingPoints = shippingPointService.getShippingPointsByManagedPageId(managedPageId);
    // ... security checks ...
}

// Format place names as "{origin} -> {place_name}"
shippingPoints.forEach(sp -> {
    if (sp.getOrigin() != null && !sp.getOrigin().isEmpty()) {
        sp.setPlaceName(sp.getOrigin() + " -> " + sp.getPlaceName());
    }
});
```

## API Usage Examples

### Get All Shipping Points for User
```bash
GET /api/shipping-points/managed-page/-1
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 1,
    "placeName": "Antananarivo -> Central Warehouse",
    "latitude": -18.879190,
    "longitude": 47.507905,
    "distance": 0.00,
    "origin": "Antananarivo",
    "managedPageId": 5
  },
  {
    "id": 2,
    "placeName": "Toamasina -> Port Office",
    "latitude": -18.143889,
    "longitude": 49.405556,
    "distance": 215.50,
    "origin": "Toamasina",
    "managedPageId": 7
  }
]
```

### Get Shipping Points for Specific Managed Page
```bash
GET /api/shipping-points/managed-page/5
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 1,
    "placeName": "Antananarivo -> Central Warehouse",
    "latitude": -18.879190,
    "longitude": 47.507905,
    "distance": 0.00,
    "origin": "Antananarivo",
    "managedPageId": 5
  }
]
```

## Benefits

1. **Flexibility:** Frontend can now easily get all shipping points or filter by managed page
2. **Better UX:** The formatted place names provide clearer context (origin + destination)
3. **Security:** Still maintains authorization checks for specific managed pages
4. **Backward Compatible:** Existing calls with valid managedPageId work as before

## Status
✅ Compiles successfully
✅ All shipping points returned when managedPageId=-1
✅ Place names formatted as {origin} -> {place_name}
✅ Security checks maintained

