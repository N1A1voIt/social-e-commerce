// orders.component.ts
import {Component, OnInit} from '@angular/core';
import {OrderService} from "./order.service";
import {OrderDisplay, OrderParent, OrderChild} from "./order.type";
import {ApiResponse} from "../inbox/inbox.service";
import {TableModule, TableRowCollapseEvent, TableRowExpandEvent, TableLazyLoadEvent} from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { RatingModule } from 'primeng/rating';
import { ToastModule } from 'primeng/toast';
import { RippleModule } from 'primeng/ripple';
import {FormsModule} from "@angular/forms";
import {CurrencyPipe, DatePipe, NgIf} from "@angular/common";
import { MessageService } from 'primeng/api';
import {FormContainerComponent} from "../../shared/form-container/form-container.component";

@Component({
  selector: 'app-orders',
  standalone: true,
  providers: [MessageService],
  imports: [TableModule, ButtonModule, TagModule, RatingModule, ToastModule, RippleModule, FormsModule, CurrencyPipe, DatePipe, NgIf, FormContainerComponent],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit{

  orders: OrderParent[] = [];   // ✅ Should be an array for p-table
  expandedRows: { [key: string]: boolean } = {};
  totalRecords: number = 0;
  loading: boolean = true;
  loadingChildren: { [key: string]: boolean } = {}; // Track loading state for child orders

  constructor(private orderService: OrderService,private messagingService:MessageService) {}

  ngOnInit(): void {
    this.fetchOrders();
  }



  fetchOrders(event?: TableLazyLoadEvent): void {
    this.loading = true;
    const page = event ? Math.floor((event.first || 0) / (event.rows || 10)) : 0;

    this.orderService.fetchAllOrders(page).subscribe({
      next: (response: ApiResponse) => {
        console.log(response);
        const orderDisplay = response.data as OrderDisplay;
        this.orders = orderDisplay.orders;
        console.log(this.orders);
        this.totalRecords = orderDisplay.totalOrders;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        console.error('Error fetching orders:', err);
        alert(err.message || 'Failed to load orders');
      }
    });
  }

  // Fetch child orders when a row is expanded
  fetchChildOrders(order: OrderParent): void {
    if (!order.idOrderM) return;

    const orderId = order.idOrderM;
    this.loadingChildren[orderId] = true;

    this.orderService.fetchOrderChild(order.idOrderM).subscribe({
      next: (response: any) => {
        const orderIndex = this.orders.findIndex(o => o.idOrderM === order.idOrderM);
        if (orderIndex !== -1) {
          this.orders[orderIndex].childs = response.data; // Adjust based on your API response structure
        }
        this.loadingChildren[orderId] = false;
      },
      error: (err) => {
        this.loadingChildren[orderId] = false;
        console.error('Error fetching child orders:', err);
        alert('Failed to load order details');
      }
    });
  }

  collapseAll(): void {
    this.expandedRows = {};
  }

  onRowExpand(event: TableRowExpandEvent): void {
    console.log('Row expanded:', event.data);
    const order = event.data as OrderParent;

    // Fetch child orders if not already loaded
    if (!order.childs || order.childs.length === 0) {
      this.fetchChildOrders(order);
    }
  }

  onRowCollapse(event: TableRowCollapseEvent): void {
    console.log('Row collapsed:', event.data);
    this.messagingService.add({ severity: 'info', summary: 'Product Expanded', detail: event.data.name, life: 3000 })
  }
   async exportOrder(order: OrderParent) {
      await this.orderService.generateOrderPdf(order);
   }
  getStatusLabel(status?: number): string {
    if (status === undefined) return 'Unknown';

    const statusMap: { [key: number]: string } = {
      1: 'Created',
      11: 'Ordered',
      21: 'Cancelled',
      31: 'Shipped',
    };

    return statusMap[status] || 'Unknown';
  }

  getStatusSeverity(status?: number | string): "success" | "info" | "danger" | "secondary" | "contrast" | "warning" | undefined {
    if (typeof status === 'string') {
      // Handle string status for child orders
      switch (status.toLowerCase()) {
        case 'shipped':
        case 'created':
        case 'ordered':
          return 'success';
        case 'processing':
        case 'cancelled':
          return 'danger';
        default:
          return 'secondary';
      }
    }

    // Handle numeric status
    switch (status) {
      case 3: // Delivered
        return 'success';
      case 1: // Processing
      case 2: // Shipped
        return 'info';
      case 0: // Pending
        return 'warning';
      case 4: // Cancelled
      case 5: // Refunded
        return 'danger';
      default:
        return 'secondary';
    }
  }

  isChildOrdersLoading(order: OrderParent): boolean {
    if (!order.idOrderM) return false;
    return this.loadingChildren[order.idOrderM.toString()] || false;
  }
}
