import { Component } from '@angular/core';
import {NgIcon} from "@ng-icons/core";
import {PostCardComponent} from "../../main/content-management/post-card/post-card.component";

interface Product {
  name: string;
  description: string;
  price: number;
  image: string;
}

@Component({
  selector: 'app-marketplace',
  standalone: true,
  imports: [
    NgIcon,
    PostCardComponent
  ],
  templateUrl: './marketplace.component.html',
  styleUrls: ['./marketplace.component.css']
})
export class MarketplaceComponent {
  products: Product[] = [
    {
      name: 'Wireless Headphones',
      description: 'High-quality wireless headphones with noise cancellation.',
      price: 99.99,
      image: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&q=80'
    },
    {
      name: 'Smart Watch',
      description: 'Track your fitness and notifications on the go.',
      price: 149.99,
      image: 'https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=crop&w=400&q=80'
    },
    {
      name: 'Eco Water Bottle',
      description: 'Reusable, BPA-free bottle for everyday hydration.',
      price: 24.99,
      image: 'https://images.unsplash.com/photo-1526178613658-3f1622045557?auto=format&fit=crop&w=400&q=80'
    },
    {
      name: 'Bluetooth Speaker',
      description: 'Portable speaker with deep bass and long battery life.',
      price: 59.99,
      image: 'https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=400&q=80'
    }
  ];
}
