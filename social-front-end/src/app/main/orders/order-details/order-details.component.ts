import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule, DatePipe, NgForOf, NgIf } from '@angular/common';
import { OrderService } from '../order.service';
import { OrderParent, OrderChild } from '../order.type';
import { ApiResponse } from '../../inbox/inbox.service';
import { FrenchNumberPipe } from '../../../shared/french-number.pipe';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { TableModule } from 'primeng/table';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@Component({
  selector: 'app-order-details',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    NgIf,
    NgForOf,
    FrenchNumberPipe,
    ButtonModule,
    TagModule,
    CardModule,
    DividerModule,
    TableModule,
    ProgressSpinnerModule,
    RouterLink
  ],
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.css']
})
export class OrderDetailsComponent implements OnInit {
  order: OrderParent | null = null;
  orderDetails:OrderChild[] = [];
  loading: boolean = true;
  error: string | null = null;
  orderId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.orderId = +params['id'];
      if (this.orderId) {
        this.loadOrderDetails();
        this.loadOrder();
      }
    });
  }
  loadOrder() {
    this.orderService.fetchOrderById(this.orderId!).subscribe({
      next: (response: ApiResponse) => {
        if (response.status === 200) {
          this.order = response.data;
          console.log('Order:', this.order);
        } else {
          this.error = response.errors?.[0]?.message || 'Failed to load order';
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading order:', err);
        this.error = 'Failed to load order. Please try again.';
        this.loading = false;
      }
    });
  }
  loadOrderDetails(): void {
    if (!this.orderId) return;

    this.loading = true;
    this.error = null;

    this.orderService.fetchOrderChild(this.orderId).subscribe({
      next: (response: ApiResponse) => {
        if (response.status === 200) {
          this.orderDetails = response.data;
          console.log('Order details:', this.orderDetails);
        } else {
          this.error = response.errors?.[0]?.message || 'Failed to load order details';
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading order details:', err);
        this.error = 'Failed to load order details. Please try again.';
        this.loading = false;
      }
    });
  }

  getStatusLabel(status?: number): string {
    switch (status) {
      case 1: return 'Pending Payment';
      case 11: return 'Paid';
      case 21: return 'In Delivery';
      case 25: return 'Delivery Available';
      case 5: return 'Completed';
      case 31: return 'Cancelled';
      default: return 'Unknown';
    }
  }

  getStatusSeverity(status?: number): 'success' | 'secondary' | 'info' | 'warning' | 'danger' | 'contrast' {
    switch (status) {
      case 1: return 'warning';
      case 11: return 'info';
      case 21: return 'info';
      case 25: return 'secondary';
      case 5: return 'success';
      case 31: return 'danger';
      default: return 'secondary';
    }
  }

  calculateItemTotal(item: OrderChild): number {
    return (item.price || 0) * (item.quantity || 0);
  }

  goBack(): void {
    this.router.navigate(['/basic/orders']);
  }

  async exportOrder(): Promise<void> {
    if (this.order) {
      await this.orderService.generateOrderPdf(this.order);
    }
  }
}
