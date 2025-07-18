import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css'
})
export class ProductDetailComponent {
  // In a real app, you would fetch product data by ID from the route
  product = {
    name: 'Wireless Headphones',
    description: 'High-quality wireless headphones with noise cancellation. Enjoy up to 30 hours of playback, fast charging, and premium comfort. Perfect for travel and work.',
    price: 99.99,
    images: [
      'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=600&q=80',
      'https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=crop&w=600&q=80',
      'https://images.unsplash.com/photo-1526178613658-3f1622045557?auto=format&fit=crop&w=600&q=80',
      'https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=600&q=80'
    ],
    seller: {
      name: 'TechStore',
      rating: 4.8,
      sales: 1200
    },
    stock: 50
  };

  selectedImage = this.product.images[0];
  quantity = 1;

  selectImage(img: string) {
    this.selectedImage = img;
  }

  addToCart() {
    alert(`Added ${this.quantity} to cart!`);
  }
}
