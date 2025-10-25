import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { javaHost } from '../../../../environments/environment';
import { Cart } from '../../cart/cart.models';

// Interfaces based on the backend DTOs

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
   * Get all active carts for the current customer
   */
  getActiveCarts(): Observable<Cart[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<Cart[]>(
      `${this.baseUrl}`,
      { headers }
    );
  }

  /**
   * Get a specific cart by ID
   */
  getCartById(cartId: number): Observable<Cart> {
    const headers = this.getAuthHeaders();
    return this.http.get<Cart>(
      `${this.baseUrl}/${cartId}`,
      { headers }
    );
  }

  /**
   * Add an item to the cart
   */
  addToCart(request: AddToCartRequest): Observable<Cart> {
    const headers = this.getAuthHeaders();
    return this.http.post<Cart>(
      `${this.baseUrl}/items`,
      request,
      { headers }
    );
  }

  /**
   * Update the quantity of an item in the cart
   */
  updateCartItem(request: UpdateCartItemRequest): Observable<Cart> {
    const headers = this.getAuthHeaders();
    return this.http.put<Cart>(
      `${this.baseUrl}/items`,
      request,
      { headers }
    );
  }

  /**
   * Remove an item from the cart
   */
  removeFromCart(variantId: number): Observable<Cart> {
    const headers = this.getAuthHeaders();
    return this.http.delete<Cart>(
      `${this.baseUrl}/items/${variantId}`,
      { headers }
    );
  }

  /**
   * Clear all items from the cart
   */
  clearCart(cartId: number): Observable<Cart> {
    const headers = this.getAuthHeaders();
    return this.http.delete<Cart>(
      `${this.baseUrl}/${cartId}/items`,
      { headers }
    );
  }
}
