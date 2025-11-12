import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService, UpdateCartItemRequest } from '../marketplace/services/cart.service';
import { CheckoutService } from '../checkout/services/checkout.service';
import { Cart, CartItem } from './cart.models';
import { FrenchNumberPipe } from '../../shared/french-number.pipe';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, FrenchNumberPipe],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  carts: Cart[] = [];
  loading = false;
  error: string | null = null;
  showCheckoutModal = false;
  selectedCart: Cart | null = null;
  checkoutForm = {
    shippingAddress: '',
    phoneNumber: ''
  };

  // To keep track of which item in which cart has an error
  errorContext: { cartId: number, variantId: number } | null = null;

  constructor(
    private cartService: CartService,
    private checkoutService: CheckoutService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCarts();
  }

  loadCarts(): void {
    this.loading = true;
    this.error = null;
    this.errorContext = null;

    this.cartService.getActiveCarts().subscribe({
      next: (carts) => {
        this.carts = carts;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading carts:', err);
        this.error = 'Failed to load carts. Please try again later.';
        this.loading = false;
      }
    });
  }

  updateQuantity(item: CartItem, newQuantity: number): void {
    if (newQuantity < 1) {
      return;
    }

    this.loading = true;

    const request: UpdateCartItemRequest = {
      variantId: item.variantId,
      quantity: newQuantity
    };

    this.cartService.updateCartItem(request).subscribe({
      next: (updatedCart) => {
        this.updateCartInList(updatedCart);
        this.loading = false;
        this.error = null;
        this.errorContext = null;
      },
      error: (err) => {
        console.error('Error updating cart item:', err);
        this.error = err.error?.message || 'Failed to update item quantity.';
        // Reload all carts to ensure data consistency
        this.loadCarts();
      }
    });
  }

  removeItem(variantId: number): void {
    if (confirm('Are you sure you want to remove this item from your cart?')) {
      this.loading = true;
      this.cartService.removeFromCart(variantId).subscribe({
        next: (updatedCart) => {
          this.updateCartInList(updatedCart);
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

  clearCart(cartId: number): void {
    if (confirm('Are you sure you want to clear this cart? This will remove all items from this seller.')) {
      this.loading = true;
      this.cartService.clearCart(cartId).subscribe({
        next: (updatedCart) => {
          // Backend might return an empty cart or just success, so we handle it by removing the cart from the list
          this.carts = this.carts.filter(c => c.cartId !== cartId);
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

  private updateCartInList(updatedCart: Cart): void {
    const index = this.carts.findIndex(c => c.cartId === updatedCart.cartId);
    if (index !== -1) {
      if (updatedCart.items.length > 0) {
        this.carts[index] = updatedCart;
      } else {
        // If the cart has no items left, remove it from the list
        this.carts.splice(index, 1);
      }
    }
  }

  get grandTotal(): number {
    return this.carts.reduce((total, cart) => total + Number(cart.totalPrice), 0);
  }

  get totalItemCount(): number {
    return this.carts.reduce((total, cart) => total + cart.itemCount, 0);
  }

  continueShopping(): void {
    this.router.navigate(['/client/marketplace']);
  }

  checkout(): void {
    // This would eventually handle checkout for multiple carts
    alert('Please use the individual checkout buttons for each seller\'s cart.');
  }

  openCheckoutModal(cart: Cart): void {
    this.selectedCart = cart;
    this.showCheckoutModal = true;
    // Reset form
    this.checkoutForm = {
      shippingAddress: '',
      phoneNumber: ''
    };
  }

  closeCheckoutModal(): void {
    this.showCheckoutModal = false;
    this.selectedCart = null;
    this.error = null;
  }

  processCheckout(): void {
    if (!this.selectedCart) {
      return;
    }

    if (!this.checkoutForm.shippingAddress.trim()) {
      this.error = 'Please enter a shipping address.';
      return;
    }

    if (!this.checkoutForm.phoneNumber.trim()) {
      this.error = 'Please enter a phone number.';
      return;
    }

    this.loading = true;
    this.error = null;
    this.checkoutService.checkout({
      sellerId: this.selectedCart.idSeller,
      shippingAddress: this.checkoutForm.shippingAddress,
      phoneNumber: this.checkoutForm.phoneNumber
    }).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.status === 200) {
          alert('Order placed successfully! Your order ID is: ' + (response.data?.idOrderM || 'N/A'));
          this.closeCheckoutModal();
          // Reload carts to show updated state
          this.loadCarts();
        } else {
          this.error = response.errors?.[0]?.message || 'Checkout failed. Please try again.';
        }
      },
      error: (err) => {
        this.loading = false;
        console.error('Checkout error:', err);
        this.error = err.error?.message || 'Failed to process checkout. Please try again.';
      }
    });
  }
}
