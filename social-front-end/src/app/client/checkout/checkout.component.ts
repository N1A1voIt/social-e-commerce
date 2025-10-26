import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CartService } from '../marketplace/services/cart.service';
import { CheckoutService } from './services/checkout.service';
import { Cart } from '../cart/cart.models';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {
  cart: Cart | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cartService: CartService,
    private checkoutService: CheckoutService
  ) {}

  ngOnInit(): void {
    const cartId = this.route.snapshot.paramMap.get('cartId');
    if (cartId) {
      this.loadCart(parseInt(cartId, 10));
    } else {
      this.error = 'No cart ID provided';
    }
  }

  loadCart(cartId: number): void {
    this.loading = true;
    this.cartService.getCartById(cartId).subscribe({
      next: (cart) => {
        this.cart = cart;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load cart details.';
        this.loading = false;
      }
    });
  }

  confirmCheckout(): void {
    if (!this.cart) return;

    this.loading = true;
    this.error = null;

    const request = { cartId: this.cart.cartId, sellerId: this.cart.sellerId };

    this.checkoutService.checkout(request).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.status === 200) {
          // Redirect to a success page or order confirmation page
          this.router.navigate(['/client/marketplace']); // TBD: redirect to order confirmation
        } else {
          this.error = 'Checkout failed. Please try again.';
        }
      },
      error: (err) => {
        this.error = 'An error occurred during checkout.';
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/client/cart']);
  }
}
