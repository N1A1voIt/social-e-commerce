import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { javaHost } from '../../../../environments/environment';

// Interfaces based on the backend DTOs
export interface CartDTO {
  cartId: number;
  customerId: number;
  createdAt: string;
  active: boolean;
  items: CartItemDTO[];
  itemCount: number;
  totalPrice: number;
}

export interface CartItemDTO {
  productId: number;
  variantId: number;
  productName: string;
  variantTitle: string;
  price: number;
  quantity: number;
  subtotal: number;
  imageUrl?: string;
}

export interface AddToCartRequest {
  productId: number;
  variantId: number;
  quantity: number;
}

export interface UpdateCartItemRequest {
  variantId: number;
  quantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private baseUrl = `${javaHost}/api/customer/cart`;

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Get the active cart for the current customer
   */
  getActiveCart(): Observable<CartDTO> {
    const headers = this.getAuthHeaders();
    return this.http.get<CartDTO>(
      `${this.baseUrl}`,
      { headers }
    );
  }

  /**
   * Get a specific cart by ID
   */
  getCartById(cartId: number): Observable<CartDTO> {
    const headers = this.getAuthHeaders();
    return this.http.get<CartDTO>(
      `${this.baseUrl}/${cartId}`,
      { headers }
    );
  }

  /**
   * Add an item to the cart
   */
  addToCart(request: AddToCartRequest): Observable<CartDTO> {
    const headers = this.getAuthHeaders();
    return this.http.post<CartDTO>(
      `${this.baseUrl}/items`,
      request,
      { headers }
    );
  }

  /**
   * Update the quantity of an item in the cart
   */
  updateCartItem(request: UpdateCartItemRequest): Observable<CartDTO> {
    const headers = this.getAuthHeaders();
    return this.http.put<CartDTO>(
      `${this.baseUrl}/items`,
      request,
      { headers }
    );
  }

  /**
   * Remove an item from the cart
   */
  removeFromCart(variantId: number): Observable<CartDTO> {
    const headers = this.getAuthHeaders();
    return this.http.delete<CartDTO>(
      `${this.baseUrl}/items/${variantId}`,
      { headers }
    );
  }

  /**
   * Clear all items from the cart
   */
  clearCart(): Observable<CartDTO> {
    const headers = this.getAuthHeaders();
    return this.http.delete<CartDTO>(
      `${this.baseUrl}/items`,
      { headers }
    );
  }
}
