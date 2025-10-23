# Frontend Development Instructions for Product Options and Variants

## Overview
This document provides instructions for building a frontend that interacts with the backend API for displaying product options and selecting variants based on option values. The frontend should allow users to:

1. View a list of products
2. View options and option values for a selected product
3. Select one value per option
4. Get the matching variant with stock information

## API Endpoints

### 1. Get All Products
```
GET /api/customer/products
```
**Headers:**
- Authorization: [customer token]

**Response:**
- List of ProductCPL objects with basic product information

### 2. Get Product Options
```
GET /api/customer/products/{productId}/options
```
**Headers:**
- Authorization: [customer token]

**Response:**
- List of ProductOptionDTO objects, each containing:
  - idOption: Long
  - label: String (e.g., "Color", "Size")
  - optionValues: List of OptionValueDTO objects, each containing:
    - idOv: Long
    - value: String (e.g., "Red", "Large")

### 3. Get Variant by Selected Option Values
```
POST /api/customer/products/variants
```
**Headers:**
- Authorization: [customer token]
- Content-Type: application/json

**Request Body:**
```json
{
  "productId": 123,
  "optionValueIds": [456, 789]
}
```

**Response:**
- VariantInStock object containing:
  - idVariant: Long
  - title: String
  - price: BigDecimal
  - idProduct: Long
  - createdAt: LocalDateTime
  - updatedAt: LocalDateTime
  - variantNumber: Double (quantity in stock)
  - stockStatus: String (e.g., "In Stock", "Out of Stock", "Low Stock")

## Authentication

All API endpoints require authentication using a customer token in the Authorization header. The frontend should:

1. Implement a login mechanism to obtain the customer token
2. Store the token securely (e.g., in localStorage or sessionStorage)
3. Include the token in all API requests
4. Handle token expiration and redirect to login if needed

## User Flow

### Product Listing Page
1. Fetch products using the GET /api/customer/products endpoint
2. Display products in a grid or list view
3. Each product should have a clickable element to view details

### Product Detail Page
1. Display basic product information
2. Fetch product options using GET /api/customer/products/{productId}/options
3. For each option:
   - Display the option label (e.g., "Color")
   - Display all option values as selectable elements (e.g., color swatches or buttons)
   - Allow the user to select exactly one value per option
4. When the user selects values for all options:
   - Collect the selected option value IDs
   - Call POST /api/customer/products/variants with the product ID and selected option value IDs
   - Display the returned variant information, including:
     - Price
     - Stock quantity
     - Stock status
5. Enable "Add to Cart" button only if the variant is in stock

## Implementation Guidelines

### Data Models

Create the following TypeScript interfaces (or equivalent in your framework):

```typescript
interface ProductCPL {
  id_product: number;
  name: string;
  description: string;
  price: number;
  media: string;
  // other fields as needed
}

interface ProductOption {
  idOption: number;
  label: string;
  optionValues: OptionValue[];
}

interface OptionValue {
  idOv: number;
  value: string;
}

interface SelectedOptionValues {
  productId: number;
  optionValueIds: number[];
}

interface VariantInStock {
  idVariant: number;
  title: string;
  price: number;
  idProduct: number;
  createdAt: string;
  updatedAt: string;
  variantNumber: number;
  stockStatus: string;
}
```

### API Service

Create an API service to handle all requests to the backend:

```typescript
class ProductService {
  private baseUrl = '/api/customer/products';
  private token: string;

  constructor(token: string) {
    this.token = token;
  }

  async getProducts(page = 0, size = 10) {
    const response = await fetch(`${this.baseUrl}?page=${page}&size=${size}`, {
      headers: {
        'Authorization': this.token
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch products');
    }
    
    return await response.json();
  }

  async getProductOptions(productId: number) {
    const response = await fetch(`${this.baseUrl}/${productId}/options`, {
      headers: {
        'Authorization': this.token
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch product options');
    }
    
    return await response.json();
  }

  async getVariantByOptionValues(productId: number, optionValueIds: number[]) {
    const response = await fetch(`${this.baseUrl}/variants`, {
      method: 'POST',
      headers: {
        'Authorization': this.token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        productId,
        optionValueIds
      })
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch variant');
    }
    
    return await response.json();
  }
}
```

### UI Components

Implement the following UI components:

1. **ProductList**: Displays a grid or list of products
2. **ProductDetail**: Displays detailed information about a product
3. **OptionSelector**: Displays an option with selectable values
4. **VariantDisplay**: Shows information about the selected variant

### Error Handling

Implement proper error handling for API requests:

1. Display loading indicators during API calls
2. Show appropriate error messages if API calls fail
3. Handle authentication errors by redirecting to login
4. Handle cases where no variant matches the selected option values

## Example Implementation (React)

Here's a simplified example of how the product detail page might be implemented in React:

```jsx
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ProductService from '../services/ProductService';

const ProductDetail = () => {
  const { productId } = useParams();
  const [product, setProduct] = useState(null);
  const [options, setOptions] = useState([]);
  const [selectedValues, setSelectedValues] = useState({});
  const [variant, setVariant] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const token = localStorage.getItem('customerToken');
  const productService = new ProductService(token);
  
  // Fetch product details and options
  useEffect(() => {
    const fetchProductData = async () => {
      try {
        setLoading(true);
        // Fetch product details (implementation depends on your API)
        // const productData = await productService.getProductById(productId);
        // setProduct(productData);
        
        // Fetch product options
        const optionsData = await productService.getProductOptions(productId);
        setOptions(optionsData);
        
        // Initialize selected values with first option value for each option
        const initialSelectedValues = {};
        optionsData.forEach(option => {
          if (option.optionValues.length > 0) {
            initialSelectedValues[option.idOption] = option.optionValues[0].idOv;
          }
        });
        setSelectedValues(initialSelectedValues);
        
        setLoading(false);
      } catch (err) {
        setError('Failed to load product data');
        setLoading(false);
      }
    };
    
    fetchProductData();
  }, [productId]);
  
  // Handle option value selection
  const handleOptionValueSelect = (optionId, valueId) => {
    setSelectedValues({
      ...selectedValues,
      [optionId]: valueId
    });
  };
  
  // Fetch variant when selected values change
  useEffect(() => {
    const fetchVariant = async () => {
      // Only proceed if we have selected values for all options
      if (options.length > 0 && Object.keys(selectedValues).length === options.length) {
        try {
          setLoading(true);
          const optionValueIds = Object.values(selectedValues);
          const variantData = await productService.getVariantByOptionValues(productId, optionValueIds);
          setVariant(variantData);
          setLoading(false);
        } catch (err) {
          setError('Failed to load variant data');
          setLoading(false);
        }
      }
    };
    
    fetchVariant();
  }, [selectedValues, options.length]);
  
  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  
  return (
    <div className="product-detail">
      {/* Product information would go here */}
      
      <div className="options-container">
        {options.map(option => (
          <div key={option.idOption} className="option">
            <h3>{option.label}</h3>
            <div className="option-values">
              {option.optionValues.map(value => (
                <button
                  key={value.idOv}
                  className={selectedValues[option.idOption] === value.idOv ? 'selected' : ''}
                  onClick={() => handleOptionValueSelect(option.idOption, value.idOv)}
                >
                  {value.value}
                </button>
              ))}
            </div>
          </div>
        ))}
      </div>
      
      {variant && (
        <div className="variant-info">
          <h3>{variant.title}</h3>
          <p>Price: ${variant.price}</p>
          <p>Stock: {variant.variantNumber} ({variant.stockStatus})</p>
          <button 
            disabled={variant.stockStatus === 'Out of Stock'}
            onClick={() => {/* Add to cart logic */}}
          >
            Add to Cart
          </button>
        </div>
      )}
    </div>
  );
};

export default ProductDetail;
```

## Best Practices

1. **State Management**: Use a state management solution (Redux, Context API, etc.) for larger applications
2. **Caching**: Implement caching for API responses to improve performance
3. **Responsive Design**: Ensure the UI works well on all device sizes
4. **Accessibility**: Make sure all UI elements are accessible
5. **Loading States**: Show appropriate loading indicators during API calls
6. **Error Handling**: Display user-friendly error messages
7. **Validation**: Validate user inputs before sending requests
8. **Testing**: Write unit and integration tests for components and services

## Conclusion

This document provides a comprehensive guide for building a frontend that interacts with the backend API for product options and variants. By following these instructions, you should be able to create a user-friendly interface that allows customers to browse products, select option values, and view matching variants with stock information.