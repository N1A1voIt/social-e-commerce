import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {StockFormComponent} from "../stock-form/stock-form.component";
import {StockMovementsComponent} from "../stock-movements/stock-movements.component";

interface StockItem {
  id: number;
  name: string;
  sku: string;
  category: string;
  currentStock: number;
  minStockLevel: number;
  status: 'In Stock' | 'Low Stock' | 'Out of Stock';
  lastUpdated: Date;
  imageUrl: string;
  stockPercentage: number;
}

@Component({
  selector: 'app-stock-list',
  standalone: true,
  imports: [CommonModule, RouterModule, StockFormComponent, StockMovementsComponent],
  templateUrl: './stock-list.component.html',
  styleUrl: './stock-list.component.css'
})
export class StockListComponent implements OnInit {
  stockItems: StockItem[] = [];
  activeTab: 'levels' | 'movements' = 'levels';

  constructor() {}

  ngOnInit(): void {
    // In a real app, this would be an HTTP request to your backend
    this.loadStockItems();
  }

  private loadStockItems(): void {
    // Mock data - replace with actual API call
    this.stockItems = [
      {
        id: 1,
        name: 'Ergonomic Chair',
        sku: 'CH-ERG-2023',
        category: 'Furniture',
        currentStock: 42,
        minStockLevel: 10,
        status: 'In Stock',
        lastUpdated: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2 hours ago
        imageUrl: 'https://demos.creative-tim.com/corporate-ui-dashboard-pro/assets/img/kam-idris-_HqHX3LBN18-unsplash.jpg',
        stockPercentage: 70
      },
      {
        id: 2,
        name: 'Standing Desk',
        sku: 'DSK-STD-2023',
        category: 'Furniture',
        currentStock: 5,
        minStockLevel: 15,
        status: 'Low Stock',
        lastUpdated: new Date(Date.now() - 5 * 60 * 60 * 1000), // 5 hours ago
        imageUrl: 'https://demos.creative-tim.com/corporate-ui-dashboard-pro/assets/img/spacejoy-NpF_OYE301E-unsplash.jpg',
        stockPercentage: 25
      },
      {
        id: 3,
        name: 'Wireless Keyboard',
        sku: 'KB-WRL-2023',
        category: 'Electronics',
        currentStock: 0,
        minStockLevel: 5,
        status: 'Out of Stock',
        lastUpdated: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // 2 days ago
        imageUrl: 'https://demos.creative-tim.com/corporate-ui-dashboard-pro/assets/img/michael-oxendine-GHCVUtBECuY-unsplash.jpg',
        stockPercentage: 0
      }
    ];
  }

  viewStockDetails(id: number): void {
    // In a real app, this would navigate to the stock detail page
    console.log(`Viewing stock details for item ${id}`);
    // this.router.navigate(['/stock', id]);
  }

  updateStock(id: number): void {
    // In a real app, this would open a modal or navigate to update page
    console.log(`Updating stock for item ${id}`);
    // this.router.navigate(['/stock', id, 'update']);
  }

  getStockStatusClass(status: string): string {
    switch (status) {
      case 'In Stock':
        return 'bg-green-100 text-green-800';
      case 'Low Stock':
        return 'bg-yellow-100 text-yellow-800';
      case 'Out of Stock':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getTimeAgo(date: Date): string {
    const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000);

    let interval = Math.floor(seconds / 31536000);
    if (interval > 1) return `${interval} years ago`;
    if (interval === 1) return '1 year ago';

    interval = Math.floor(seconds / 2592000);
    if (interval > 1) return `${interval} months ago`;
    if (interval === 1) return '1 month ago';

    interval = Math.floor(seconds / 86400);
    if (interval > 1) return `${interval} days ago`;
    if (interval === 1) return 'yesterday';

    interval = Math.floor(seconds / 3600);
    if (interval >= 1) return `${interval} hour${interval === 1 ? '' : 's'} ago`;

    interval = Math.floor(seconds / 60);
    if (interval >= 1) return `${interval} minute${interval === 1 ? '' : 's'} ago`;

    return 'just now';
  }
}
