import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CustomerOrdersService, CustomerOrder } from './orders.service';

@Component({
  selector: 'app-client-orders',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class ClientOrdersComponent implements OnInit {
  orders: CustomerOrder[] = [];
  loading = false;
  error: string | null = null;

  constructor(private ordersService: CustomerOrdersService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = null;

    this.ordersService.getOrders().subscribe({
      next: (response) => {
        if (response.status === 200) {
          this.orders = response.data || [];
        } else {
          this.error = response.errors?.[0]?.message || 'Failed to load orders.';
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading orders:', err);
        this.error = err.error?.message || 'Failed to load orders. Please try again later.';
        this.loading = false;
      }
    });
  }

  getStatusColor(status: number): string {
    const statusColors: { [key: number]: string } = {
      1: 'bg-yellow-100 text-yellow-800', // Pending
      2: 'bg-blue-100 text-blue-800',     // Confirmed
      3: 'bg-purple-100 text-purple-800',  // Processing
      4: 'bg-indigo-100 text-indigo-800',   // Shipped
      5: 'bg-green-100 text-green-800',    // Delivered
      6: 'bg-red-100 text-red-800'         // Cancelled
    };
    return statusColors[status] || 'bg-gray-100 text-gray-800';
  }
}

