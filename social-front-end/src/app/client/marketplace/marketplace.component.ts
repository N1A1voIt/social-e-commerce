import { Component, OnInit } from '@angular/core';
import { NgIcon } from "@ng-icons/core";
import { PostCardComponent } from "../../main/content-management/post-card/post-card.component";
import { ShopCardComponent } from "./shop-card/shop-card.component";
import {NgForOf, NgIf} from "@angular/common";
import { CustomerProductService, ProductCPL } from './services/customer-product.service';

@Component({
  selector: 'app-marketplace',
  standalone: true,
  imports: [
    NgIcon,
    PostCardComponent,
    ShopCardComponent,
    NgForOf,
    NgIf
  ],
  templateUrl: './marketplace.component.html',
  styleUrls: ['./marketplace.component.css']
})
export class MarketplaceComponent implements OnInit {
  products: ProductCPL[] = [];
  loading = false;
  error: string | null = null;

  constructor(private productService: CustomerProductService) {}

  ngOnInit(): void {
    this.fetchProducts();
  }

  fetchProducts(): void {
    this.loading = true;
    this.error = null;

    this.productService.getProducts()
      .subscribe({
        next: (data) => {
          this.products = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error fetching products:', err);
          this.error = 'Failed to load products. Please try again later.';
          this.loading = false;

          // Fallback to sample data for development
          this.products = [
            {
              idPc: 1,
              name: 'Wireless Headphones',
              description: 'High-quality wireless headphones with noise cancellation.',
              price: 99.99,
              media: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&q=80'
            },
            {
              idPc: 2,
              name: 'Smart Watch',
              description: 'Track your fitness and notifications on the go.',
              price: 149.99,
              media: 'https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=crop&w=400&q=80'
            },
            {
              idPc: 3,
              name: 'Eco Water Bottle',
              description: 'Reusable, BPA-free bottle for everyday hydration.',
              price: 24.99,
              media: 'https://images.unsplash.com/photo-1526178613658-3f1622045557?auto=format&fit=crop&w=400&q=80'
            },
            {
              idPc: 4,
              name: 'Bluetooth Speaker',
              description: 'Portable speaker with deep bass and long battery life.',
              price: 59.99,
              media: 'https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=400&q=80'
            }
          ];
        }
      });
  }
}
