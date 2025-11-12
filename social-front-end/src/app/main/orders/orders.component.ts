// orders.component.ts
import {Component, OnInit} from '@angular/core';
import {OrderService} from "./order.service";
import {OrderDisplay, OrderParent, DeliveryMission, DeliveryApplicant, RefundRequest, Refund} from "./order.type";
import {ApiResponse} from "../inbox/inbox.service";
import {TableModule, TableRowCollapseEvent, TableRowExpandEvent, TableLazyLoadEvent} from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { RatingModule } from 'primeng/rating';
import { ToastModule } from 'primeng/toast';
import { RippleModule } from 'primeng/ripple';
import {FormsModule} from "@angular/forms";
import {DecimalPipe, DatePipe, NgForOf, NgIf} from "@angular/common";
import { MessageService } from 'primeng/api';
import {FormContainerComponent} from "../../shared/form-container/form-container.component";
import {BeautifulButtonComponent} from "../../shared/beautiful-button/beautiful-button.component";
import {BasicSelectComponent} from "../../shared/basic-select/basic-select.component";
import {ShippingPointService} from "../settings/managed-account/shipping-point/shipping-point.service";
import {ShippingPoint} from "../settings/managed-account/shipping-point/shipping-point.model";
import {SelectOption} from "../../shared/basic-select/basic-select.component";
import {FrenchNumberPipe} from "../../shared/french-number.pipe";
import { CalendarModule } from 'primeng/calendar';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import {BasicInputComponent} from "../../shared/basic-input/basic-input.component";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-orders',
  standalone: true,
  providers: [MessageService],
  imports: [TableModule, ButtonModule, TagModule, RatingModule, ToastModule, RippleModule, FormsModule, DecimalPipe, DatePipe, NgIf, FormContainerComponent, BeautifulButtonComponent, BasicSelectComponent, NgForOf, FrenchNumberPipe, CalendarModule, InputTextModule, DropdownModule, BasicInputComponent, RouterLink],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit{
  showApplicants!:boolean;
  orders: OrderParent[] = [];   // ✅ Should be an array for p-table
  expandedRows: { [key: string]: boolean } = {};
  totalRecords: number = 0;
  loading: boolean = true;
  activeOrder?: OrderParent;
  loadingChildren: { [key: string]: boolean } = {}; // Track loading state for child orders
  step:string = '1'; // 1 - payment , 2 - delivery
  openModal: boolean = false;
  applicants:DeliveryApplicant[] = [];
  // Shipping points related properties
  shippingPoints: ShippingPoint[] = [];
  shippingPointOptions: SelectOption[] = [];
  selectedShippingPoint: number | null = null;
  loadingShippingPoints: boolean = false;

  // Cancel order related properties
  showCancelModal: boolean = false;
  refundInfo: Refund | null = null;
  cancellingOrder: OrderParent | null = null;

  // Payment method selection (for status 41 - Delivered - Asking for full payment)
  selectedPaymentMethod: string | null = null;
  paymentMethodOptions: SelectOption[] = [
    { label: 'MVola', value: 'mvola' },
    { label: 'Cash', value: 'cash' }
  ];

  // Filter properties
  filterStatus: number | null = null;
  filterCustomerName: string = '';
  filterStartDate: Date | null = null;
  filterEndDate: Date | null = null;

  // Status options for dropdown
  statusOptions: SelectOption[] = [
    { label: 'All Statuses', value: null },
    { label: 'Created', value: 1 },
    { label: 'Ordered', value: 11 },
    { label: 'Waiting for deliverer', value: 25 },
    { label: 'Cancelled', value: 21 },
    { label: 'Completed', value: 5 },
    { label: 'Shipped', value: 31 }
  ];

  constructor(
    private orderService: OrderService,
    private messagingService: MessageService,
    private shippingPointService: ShippingPointService
  ) {}

  ngOnInit(): void {
    this.fetchOrders();
  }

  sendBillingAndPaymentLink() {

    this.orderService.sendBillingAndPaymentLink(this.activeOrder!).subscribe({
        next: (response: ApiResponse) => {
          console.log(response);
          this.openModal = false;
          this.fetchOrders();
        },error(err:ApiResponse) {
          alert(err.errors[0].message);
          // .messagingService.add({severity: 'error', summary: 'Error', detail: err.errors[0].message});
        }
    });
  }


  nextStep(order:OrderParent) {
    this.activeOrder = order;
    this.selectedShippingPoint = null;

    // If status is 31 (Cancelled), don't open modal, just notify customer
    if(order.dstatus == 31) {
       this.notifyCustomer(order.idOrderM || -1);
       console.log("Salame")
       return; // Exit early
    }

    // Open modal for other statuses
    this.openModal = true;

    if (order.dstatus == 1) {
      console.log(order.dstatus);
      this.step = '1';
    } else if (order.dstatus == 11) {
      this.step = '2';
      // Fetch child orders if not already loaded to get the managed page ID
      if (!order.childs || order.childs.length === 0) {
        this.fetchChildOrders(order, () => {
          this.fetchShippingPointsForOrder(order);
        });
      } else {
        this.fetchShippingPointsForOrder(order);
      }
    } else if (order.dstatus == 41) {
      // Delivered - Asking for full payment
      this.step = '3';
      this.selectedPaymentMethod = null; // Reset payment method selection
    }
  }
  notifyCustomer(orderId:number) {
    this.orderService.notifyCompleteDelivery(orderId).subscribe({
      next: (response: ApiResponse) => {
        console.log(response);
        this.fetchOrders();
      },error(err:ApiResponse) {
        alert(err.errors[0].message);
      }
    });
  }
  fetchShippingPointsForOrder(order: OrderParent) {
    if (!order.childs || order.childs.length === 0) {
      this.messagingService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No order details found to determine shipping points'
      });
      return;
    }
    console.log(order)

    // Get the first child order with a managed page ID
    // const childWithManagedPage = order.childs.find(child => child.idManagedPages);
    //
    // if (!childWithManagedPage || !childWithManagedPage.idManagedPages) {
    //   this.messagingService.add({
    //     severity: 'error',
    //     summary: 'Error',
    //     detail: 'No managed page associated with this order'
    //   });
    //   return;
    // }

    this.loadingShippingPoints = true;
    // console.log(childWithManagedPage)
    this.shippingPointService.getShippingPointsByManagedPageId(order?.idManagedPages || -1)
      .subscribe({
        next: (points: ShippingPoint[]) => {
          this.shippingPoints = points;
          this.shippingPointOptions = this.convertToSelectOptions(points);
          this.loadingShippingPoints = false;
        },
        error: (err) => {
          console.error('Error fetching shipping points:', err);
          this.messagingService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load shipping points'
          });
          this.loadingShippingPoints = false;
        }
      });
  }

  convertToSelectOptions(points: ShippingPoint[]): SelectOption[] {
    return points.map(point => ({
      value: point.id,
      label: point.placeName,
      description: `Distance: ${point.distance} km`
    }));
  }

  sendMission() {
    if (!this.selectedShippingPoint) {
      this.messagingService.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'Please select a shipping point'
      });
      return;
    }
    let orderMission:DeliveryMission = {
      orderParent:this.activeOrder!,
      shippingPointId:this.selectedShippingPoint
    }
    this.orderService.sendMission(orderMission).subscribe({
      next: (response:ApiResponse) => {
        console.log(response);
        this.openModal = false;
        this.fetchOrders();
      },error(err:ApiResponse) {
        alert(err.errors[0].message);
      }
    });
    // this.orderService.sendMission(
    // Here you would implement the actual API call to send the mission
    // with the selected shipping point and order details
    console.log('Sending mission with shipping point:', this.selectedShippingPoint);
    console.log('For order:', this.activeOrder);

    this.messagingService.add({
      severity: 'success',
      summary: 'Success',
      detail: 'Mission sent to delivery persons'
    });

    this.openModal = false;
  }

  confirmPayment() {
    if (!this.selectedPaymentMethod) {
      this.messagingService.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'Please select a payment method'
      });
      return;
    }

    if (!this.activeOrder || !this.activeOrder.idOrderM) {
      this.messagingService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No active order found'
      });
      return;
    }

    // Call API to confirm payment with selected method
    this.orderService.confirmFullPayment(this.activeOrder.idOrderM, this.selectedPaymentMethod).subscribe({
      next: (response: ApiResponse) => {
        console.log('Payment confirmed:', response);
        this.messagingService.add({
          severity: 'success',
          summary: 'Success',
          detail: `Payment confirmed via ${this.selectedPaymentMethod === 'mvola' ? 'MVola' : 'Cash'}`
        });
        this.openModal = false;
        this.fetchOrders();
      },
      error: (err: any) => {
        console.error('Error confirming payment:', err);
        this.messagingService.add({
          severity: 'error',
          summary: 'Error',
          detail: err.error?.errors?.[0]?.message || 'Failed to confirm payment'
        });
      }
    });
  }

  fetchOrders(event?: TableLazyLoadEvent): void {
    this.loading = true;
    const page = event ? Math.floor((event.first || 0) / (event.rows || 10)) : 0;

    // Format dates to ISO string if they exist
    const startDateStr = this.filterStartDate ? this.formatDateToISO(this.filterStartDate) : null;
    const endDateStr = this.filterEndDate ? this.formatDateToISO(this.filterEndDate) : null;

    // Clean up customer name - convert empty string to null
    const customerNameFilter = this.filterCustomerName && this.filterCustomerName.trim() !== ''
      ? this.filterCustomerName.trim()
      : null;

    this.orderService.fetchAllOrders(
      page,
      this.filterStatus,
      customerNameFilter,
      startDateStr,
      endDateStr
    ).subscribe({
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

  formatDateToISO(date: Date): string {
    // Format date to ISO 8601 format expected by backend
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  }

  applyFilters(): void {
    // Reset to first page when applying filters
    this.fetchOrders();
  }

  clearFilters(): void {
    this.filterStatus = null;
    this.filterCustomerName = '';
    this.filterStartDate = null;
    this.filterEndDate = null;
    this.fetchOrders();
  }

  // Fetch child orders when a row is expanded
  fetchChildOrders(order: OrderParent, callback?: () => void): void {
    if (!order.idOrderM) return;

    const orderId = order.idOrderM;
    this.loadingChildren[orderId] = true;

    this.orderService.fetchOrderChild(order.idOrderM).subscribe({
      next: (response: any) => {
        const orderIndex = this.orders.findIndex(o => o.idOrderM === order.idOrderM);
        if (orderIndex !== -1) {
          this.orders[orderIndex].childs = response.data; // Adjust based on your API response structure
        }

        // If this is the active order, update it directly
        if (this.activeOrder && this.activeOrder.idOrderM === order.idOrderM) {
          this.activeOrder.childs = response.data;
        }

        this.loadingChildren[orderId] = false;

        // Call the callback if provided
        if (callback) {
          callback();
        }
      },
      error: (err) => {
        this.loadingChildren[orderId] = false;
        console.error('Error fetching child orders:', err);
        this.messagingService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load order details'
        });
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
    this.messagingService.add({ severity: 'info', summary: 'Order Collapsed', detail: `Order #${event.data.idOrderM}`, life: 3000 })
  }
   async exportOrder(order: OrderParent) {
      await this.orderService.generateOrderPdf(order);
   }
   viewApplicants(order: OrderParent) {

     if (order.idOrderM != null) {
       this.orderService.fetchApplicants(order.idOrderM).subscribe({
         next: (response: ApiResponse) => {
           console.log(response);
          this.showApplicants = true;
          this.applicants = response.data;
         }, error: (err: ApiResponse) => {
          alert(err);
         }
       });
     }
   }
  getStatusLabel(status?: number): string {
    if (status === undefined) return 'Unknown';

    const statusMap: { [key: number]: string } = {
      1: 'Created',
      5: 'Waiting for payment',
      11: 'Ordered',
      21: 'Cancelled',
      25: 'Waiting for deliverer',
      31: 'Shipped',
      41: 'Delivered - Asking for full payment',
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
  assignDriver(applicant:DeliveryApplicant) {
    this.orderService.assignDriver(applicant).subscribe({
      next: (response: ApiResponse) => {
        this.showApplicants = false;
        this.messagingService.add({ severity: 'info', summary: 'Deliverer assigned', detail: `You have assigned ${applicant.driverName} to this order`, life: 3000 })
      },
      error: (err: any) => {
        this.messagingService.add({ severity: 'error', summary: 'An error has occured', detail: `${err.error.errors[0].message}`, life: 3000 })
      },
    })
  }

  cancelOrder(order: OrderParent) {
    if (!order.idOrderM) {
      this.messagingService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Invalid order'
      });
      return;
    }

    this.cancellingOrder = order;

    // If status >= 11, call the API to get refund info and show modal
    if (order.dstatus && order.dstatus >= 11) {
      const refundRequest: RefundRequest = {
        orderId: order.idOrderM,
        amount: order.dtotal || 0
      };

      this.orderService.cancelOrder(refundRequest).subscribe({
        next: (response: ApiResponse) => {
          this.refundInfo = response.data as Refund;
          this.showCancelModal = true;
        },
        error: (err: any) => {
          console.error('Error getting refund info:', err);
          const errorMessage = err.error?.errors?.[0]?.message || 'Failed to process cancellation';
          this.messagingService.add({
            severity: 'error',
            summary: 'Error',
            detail: errorMessage
          });
        }
      });
    } else {
      // If status < 11, proceed with cancellation directly (no refund needed)
      const refundRequest: RefundRequest = {
        orderId: order.idOrderM,
        amount: 0
      };

      this.orderService.cancelOrder(refundRequest).subscribe({
        next: (response: ApiResponse) => {
          this.messagingService.add({
            severity: 'success',
            summary: 'Order Cancelled',
            detail: 'The order has been successfully cancelled'
          });
          this.cancellingOrder = null;
          this.fetchOrders(); // Refresh the orders list
        },
        error: (err: any) => {
          console.error('Error cancelling order:', err);
          const errorMessage = err.error?.errors?.[0]?.message || 'Failed to cancel order';
          this.messagingService.add({
            severity: 'error',
            summary: 'Error',
            detail: errorMessage
          });
          this.cancellingOrder = null;
        }
      });
    }
  }

  closeRefundModal() {
    this.showCancelModal = false;
    this.cancellingOrder = null;
    this.refundInfo = null;
    this.fetchOrders(); // Refresh the orders list
  }
}
