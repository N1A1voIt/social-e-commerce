import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService, CartDTO, CartItemDTO, UpdateCartItemRequest } from '../marketplace/services/cart.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  cart: CartDTO | null = null;
  loading = false;
  error: string | null = null;
  isStockError = false;
  errorItem: CartItemDTO | null = null;

  constructor(
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.loading = true;
    this.error = null;
    this.isStockError = false;
    this.errorItem = null;

    this.cartService.getActiveCart().subscribe({
      next: (cart) => {
        this.cart = cart;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading cart:', err);
        this.error = 'Failed to load cart. Please try again later.';
        this.loading = false;
      }
    });
  }

  tryWithUpdatedQuantity(item: CartItemDTO): void {
    if (item && item.quantity > 0) {
      this.updateQuantity(item, item.quantity);
    }
  }

  updateQuantity(item: CartItemDTO, newQuantity: number): void {
    if (newQuantity < 1) {
      return;
    }

    this.loading = true;
    this.error = null;
    this.isStockError = false;
    this.errorItem = null;

    const request: UpdateCartItemRequest = {
      variantId: item.variantId,
      quantity: newQuantity
    };

    this.cartService.updateCartItem(request).subscribe({
      next: (cart) => {
        this.cart = cart;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error updating cart item:', err);

        // Check if the error is related to insufficient stock
        if (err.error && err.error.message && err.error.message.includes('Cannot update cart') && err.error.message.includes('items available in stock')) {
          this.error = err.error.message;
          this.isStockError = true;
          this.errorItem = item; // Store the item that had the error

          // Extract available stock from error message if possible
          const stockMatch = err.error.message.match(/Only ([0-9.]+) items available in stock/);
          if (stockMatch && stockMatch[1]) {
            const availableStock = parseFloat(stockMatch[1]);
            if (availableStock > 0) {
              // Update the item's quantity in the UI to match available stock
              item.quantity = Math.floor(availableStock);
            }
          }
        } else {
          this.error = 'Failed to update cart item. Please try again later.';
          this.isStockError = false;
          this.errorItem = null;
        }

        // Reset the cart to its previous state
        this.loadCart();
        this.loading = false;
      }
    });
  }

  removeItem(variantId: number): void {
    if (confirm('Are you sure you want to remove this item from your cart?')) {
      this.loading = true;
      this.cartService.removeFromCart(variantId).subscribe({
        next: (cart) => {
          this.cart = cart;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error removing cart item:', err);
          this.error = 'Failed to remove cart item. Please try again later.';
          this.loading = false;
        }
      });
    }
  }

  clearCart(): void {
    if (confirm('Are you sure you want to clear your cart? This will remove all items.')) {
      this.loading = true;
      this.cartService.clearCart().subscribe({
        next: (cart) => {
          this.cart = cart;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error clearing cart:', err);
          this.error = 'Failed to clear cart. Please try again later.';
          this.loading = false;
        }
      });
    }
  }

  continueShopping(): void {
    this.router.navigate(['/client/marketplace']);
  }

  checkout(): void {
    // Navigate to checkout page (to be implemented)
    alert('Checkout functionality will be implemented in the future.');
    // this.router.navigate(['/client/checkout']);
  }
}
