import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { javaHost } from '../../../../environments/environment';
import {BasicInputComponent} from "../../../shared/basic-input/basic-input.component";
import {BasicSelectComponent, SelectOption} from "../../../shared/basic-select/basic-select.component";
import {BasicButtonComponent} from "../../../shared/basic-button/basic-button.component";
import {FrenchNumberPipe} from "../../../shared/french-number.pipe";

export interface StockMovement {
  idStCh: number;
  actionAt: string;
  input?: number;
  output?: number;
  price: number;
  productStockAfter: number;
  variantStockAfter: number;
  createdAt: string;
  idProduct: number;
  productName: string;
  skuPrefix: string;
  productMedia?: string;
  idSeller: number;
  idVariant: number;
  variantName: string;
  variantSku?: string;
  variantMedia?: string;
  idCategory: number;
  categoryName: string;
  idMv: number;
  movementDescription?: string;
  idOrderM?: number;
  customerName?: string;
  orderStatus?: string;
  movementType: 'STOCK_IN' | 'STOCK_OUT' | 'ADJUSTMENT' | 'UNKNOWN';
  netMovement: number;
}

export interface StockMovementPage {
  content: StockMovement[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

@Component({
  selector: 'app-stock-movements',
  standalone: true,
  imports: [CommonModule, FormsModule, BasicInputComponent, BasicSelectComponent, BasicButtonComponent, FrenchNumberPipe],
  template: `
    <div class="w-full h-full bg-white p-6 rounded-3xl overflow-y-scroll hide-scrollbar">
      <!-- Header -->
      <div class="w-full flex justify-between items-center mb-6">
        <div>
          <h3 class="text-lg font-semibold text-slate-800">Stock Movements</h3>
          <p class="text-slate-500">Track all inventory movements and transactions.</p>
        </div>
        <div class="flex gap-3">
          <app-basic-button
            (click)="exportCsv()"
            type="secondary"
            [disabled]="loading || exporting"
            class="flex items-center gap-2">
            <i class="pi pi-file-export" *ngIf="!exporting"></i>
            <i class="pi pi-spin pi-spinner" *ngIf="exporting"></i>
            Export CSV
          </app-basic-button>
          <app-basic-button
            (click)="exportPdf()"
            type="secondary"
            [disabled]="loading || exporting"
            class="flex items-center gap-2">
            <i class="pi pi-file-pdf" *ngIf="!exporting"></i>
            <i class="pi pi-spin pi-spinner" *ngIf="exporting"></i>
            Export PDF
          </app-basic-button>
        </div>
      </div>

      <!-- Filters -->
      <div class="w-full px-4 bg-gray-50 rounded-lg p-4 border-4 border-blue-50">
        <div class="grid grid-cols-1 md:grid-cols-5 gap-4 mb-3">
          <!-- Search -->
          <div class="flex flex-col gap-2">
            <label class="text-md text-gray-700">Search</label>
            <app-basic-input
              [(ngModel)]="filterSearch"
              (ngModelChange)="applyFilters()"
              placeholder="Product name, SKU..."
              type="text">
            </app-basic-input>
          </div>

          <!-- Movement Type -->
          <div class="flex flex-col gap-2">
            <label class="text-md text-gray-700">Movement Type</label>
            <app-basic-select
              [(ngModel)]="filterMovementType"
              [options]="movementTypeOptions"
              (ngModelChange)="applyFilters()"
              placeholder="All Types">
            </app-basic-select>
          </div>

          <!-- Start Date -->
          <div class="flex flex-col gap-2">
            <label class="text-md text-gray-700">Start Date</label>
            <app-basic-input
              [(ngModel)]="filterStartDate"
              (ngModelChange)="applyFilters()"
              type="date">
            </app-basic-input>
          </div>

          <!-- End Date -->
          <div class="flex flex-col gap-2">
            <label class="text-md text-gray-700">End Date</label>
            <app-basic-input
              [(ngModel)]="filterEndDate"
              (ngModelChange)="applyFilters()"
              type="date">
            </app-basic-input>
          </div>

          <!-- Clear Filters Button -->
          <div class="flex flex-col gap-2 justify-end">
            <app-basic-button
              (click)="clearFilters()"
              type="secondary"
              class="h-10">
              Clear Filters
            </app-basic-button>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div *ngIf="loading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <span class="ml-3 text-gray-600">Loading stock movements...</span>
      </div>

      <!-- Error State -->
      <div *ngIf="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4">
        {{ error }}
      </div>

      <!-- Stock Movements Cards -->
      <div class="w-full p-4 rounded-xl flex flex-col gap-4" *ngIf="!loading">
        <div class="border-4 border-blue-50 rounded-xl" *ngFor="let movement of movements">
          <div class="flex items-center gap-4 p-4 hover:bg-muted/50 transition-colors">
            <!-- Date/Time -->
            <div class="w-24">
              <div class="text-sm text-gray mb-1">Date</div>
              <div class="font-mono text-md">{{ formatDate(movement.actionAt) }}</div>
              <div class="text-xs text-gray">{{ formatTime(movement.actionAt) }}</div>
            </div>

            <!-- Product -->
            <div class="flex-1 min-w-0">
              <div class="text-sm text-gray mb-1">Product</div>
              <div class="flex items-center gap-3">
                <div class="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center flex-shrink-0">
                  <img
                    *ngIf="movement.productMedia"
                    [src]="movement.productMedia"
                    [alt]="movement.productName"
                    class="h-10 w-10 rounded-full object-cover"
                    onError="this.style.display='none'">
                  <span
                    *ngIf="!movement.productMedia"
                    class="text-gray-500 text-sm font-medium">
                    {{ movement.productName?.charAt(0)?.toUpperCase() }}
                  </span>
                </div>
                <div class="min-w-0">
                  <div class="text-md truncate" [title]="movement.productName">{{ movement.productName }}</div>
                  <div class="text-xs text-gray truncate">{{ movement.skuPrefix }}</div>
                </div>
              </div>
            </div>

            <!-- Variant -->
            <div class="flex-1 min-w-0">
              <div class="text-sm text-gray mb-1">Variant</div>
              <div class="text-md truncate" [title]="movement.variantName">{{ movement.variantName }}</div>
              <div class="text-xs text-gray truncate" *ngIf="movement.variantSku">{{ movement.variantSku }}</div>
            </div>

            <!-- Movement -->
            <div class="flex-1 min-w-0">
              <div class="text-sm text-gray mb-1">Movement</div>
              <div class="text-md font-medium"
                   [ngClass]="{
                     'text-green-600': movement.netMovement > 0,
                     'text-red-600': movement.netMovement < 0,
                     'text-gray-600': movement.netMovement === 0
                   }">
                <span *ngIf="movement.netMovement > 0">+</span>{{ movement.netMovement }}
              </div>
            </div>

            <!-- Type -->
            <div class="flex-1 min-w-0">
              <div class="text-sm text-gray mb-1">Type</div>
              <div class="flex items-center gap-2">
                <span [ngClass]="getMovementColor(movement.movementType)"
                      class="status-badge inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium border">
                  <i [class]="'pi ' + getMovementIcon(movement.movementType)" class="text-xs"></i>
                  {{ getMovementTypeLabel(movement.movementType) }}
                </span>
              </div>
            </div>

            <!-- Stock After -->
            <div class="flex-1 min-w-0">
              <div class="text-sm text-gray mb-1">Stock After</div>
              <div class="text-md truncate">P: {{ movement.productStockAfter }}</div>
              <div class="text-xs text-gray truncate">V: {{ movement.variantStockAfter }}</div>
            </div>

            <!-- Order -->
            <div class="flex-1 min-w-0">
              <div class="text-sm text-gray mb-1">Order</div>
              <div
                class="text-md truncate cursor-pointer hover:text-blue-600 hover:underline transition-colors"
                *ngIf="movement.idOrderM"
                [title]="'Order #' + movement.idOrderM + ' - Click to view details'"
                (click)="navigateToOrder(movement.idOrderM)">
                ORD{{ movement.idOrderM }}
              </div>
              <div class="text-xs text-gray truncate" *ngIf="movement.customerName">{{ movement.customerName }}</div>
              <div class="text-md text-gray-400" *ngIf="!movement.idOrderM">-</div>
            </div>

            <!-- Price -->
            <div class="flex-1 min-w-0">
              <div class="text-sm text-gray mb-1">Price</div>
              <div class="text-md truncate">{{ movement.price | frenchNumber }} Ar</div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div *ngIf="movements.length === 0" class="text-center py-12 border-4 border-blue-50 rounded-xl">
          <div class="text-gray-500">
            <i class="pi pi-inbox text-4xl mb-4"></i>
            <h3 class="text-lg font-medium mb-2">No stock movements found</h3>
            <p>Try adjusting your filters or check back later.</p>
          </div>
        </div>
      </div>      <!-- Pagination -->
      <div class="flex items-center justify-between mt-6" *ngIf="totalPages > 1">
        <div class="text-sm text-gray-700">
          Showing {{ (currentPage * pageSize) + 1 }} to {{ Math.min((currentPage + 1) * pageSize, totalElements) }} of {{ totalElements }} results
        </div>
        <div class="flex space-x-2">
          <button
            (click)="goToPage(currentPage - 1)"
            [disabled]="currentPage === 0"
            [ngClass]="currentPage === 0 ? 'bg-gray-300 cursor-not-allowed' : 'bg-blue-500 hover:bg-blue-600'"
            class="px-3 py-1 text-white rounded">
            Previous
          </button>
          <span class="px-3 py-1 bg-gray-100 rounded">
            Page {{ currentPage + 1 }} of {{ totalPages }}
          </span>
          <button
            (click)="goToPage(currentPage + 1)"
            [disabled]="currentPage >= totalPages - 1"
            [ngClass]="currentPage >= totalPages - 1 ? 'bg-gray-300 cursor-not-allowed' : 'bg-blue-500 hover:bg-blue-600'"
            class="px-3 py-1 text-white rounded">
            Next
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .hide-scrollbar {
      -ms-overflow-style: none;
      scrollbar-width: none;
    }
    .hide-scrollbar::-webkit-scrollbar {
      display: none;
    }
    .status-badge {
      transition: all 0.2s ease-in-out;
      cursor: default;
      user-select: none;
    }
    .status-badge:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    .text-gray {
      color: #6b7280;
    }
    .bg-muted\/50 {
      background-color: rgba(241, 245, 249, 0.5);
    }
  `]
})
export class StockMovementsComponent implements OnInit {
  movements: StockMovement[] = [];
  loading = false;
  exporting = false;
  error: string | null = null;

  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalPages = 0;
  totalElements = 0;

  // Filters
  filterSearch = '';
  filterMovementType = '';
  filterStartDate = '';
  filterEndDate = '';

  // Filter Options
  movementTypeOptions: SelectOption[] = [
    { label: 'All Types', value: '' },
    { label: 'Stock In', value: 'STOCK_IN' },
    { label: 'Stock Out', value: 'STOCK_OUT' },
    { label: 'Adjustment', value: 'ADJUSTMENT' }
  ];

  // Helper for Math functions in template
  Math = Math;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadStockMovements();
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', token || '');
  }

  loadStockMovements() {
    this.loading = true;
    this.error = null;

    let url = `${javaHost}/api/stock-movements?page=${this.currentPage}&size=${this.pageSize}`;

    // Add filters
    if (this.filterSearch.trim()) {
      url += `&search=${encodeURIComponent(this.filterSearch.trim())}`;
    }
    if (this.filterMovementType) {
      url += `&movementType=${this.filterMovementType}`;
    }
    if (this.filterStartDate && this.filterEndDate) {
      url += `&startDate=${this.filterStartDate}T00:00:00&endDate=${this.filterEndDate}T23:59:59`;
    }

    this.http.get<any>(url, { headers: this.getAuthHeaders() }).subscribe({
      next: (response) => {
          console.log(response)
        if (response.data) {
          const page = response.data as StockMovementPage;
          this.movements = page.content;
          console.log("Hello")
          this.totalPages = page.totalPages;
          this.totalElements = page.totalElements;
        } else {
          this.error = response.message || 'Failed to load stock movements';
          this.movements = [];
        }

        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading stock movements:', error);
        this.error = 'Failed to load stock movements. Please try again.';
        this.loading = false;
      }
    });
  }

  applyFilters() {
    this.currentPage = 0; // Reset to first page when filtering
    this.loadStockMovements();
  }

  clearFilters() {
    this.filterSearch = '';
    this.filterMovementType = '';
    this.filterStartDate = '';
    this.filterEndDate = '';
    this.applyFilters();
  }

  goToPage(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadStockMovements();
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  formatTime(dateString: string): string {
    return new Date(dateString).toLocaleTimeString();
  }

  getMovementTypeLabel(type: string): string {
    switch (type) {
      case 'STOCK_IN': return 'Stock In';
      case 'STOCK_OUT': return 'Stock Out';
      case 'ADJUSTMENT': return 'Adjustment';
      default: return 'Unknown';
    }
  }

  getMovementColor(type: string): string {
    switch (type) {
      case 'STOCK_IN': return 'bg-green-100 text-green-800 border-green-200';
      case 'STOCK_OUT': return 'bg-red-100 text-red-800 border-red-200';
      case 'ADJUSTMENT': return 'bg-blue-100 text-blue-800 border-blue-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  }

  getMovementIcon(type: string): string {
    switch (type) {
      case 'STOCK_IN': return 'pi-arrow-up';
      case 'STOCK_OUT': return 'pi-arrow-down';
      case 'ADJUSTMENT': return 'pi-refresh';
      default: return 'pi-question';
    }
  }

  exportCsv() {
    if (this.totalElements === 0) {
      this.error = 'No data available to export. Please adjust your filters or add some stock movements.';
      return;
    }
    this.downloadFile('csv');
  }

  exportPdf() {
    if (this.totalElements === 0) {
      this.error = 'No data available to export. Please adjust your filters or add some stock movements.';
      return;
    }
    this.downloadFile('pdf');
  }

  private downloadFile(format: 'csv' | 'pdf') {
    this.exporting = true;
    this.error = null;

    const headers = this.getAuthHeaders();
    let url = `${javaHost}/api/stock-movements/export/${format}?`;

    // Add current filters to export
    const params: string[] = [];
    if (this.filterSearch.trim()) {
      params.push(`search=${encodeURIComponent(this.filterSearch.trim())}`);
    }
    if (this.filterMovementType) {
      params.push(`movementType=${this.filterMovementType}`);
    }
    if (this.filterStartDate && this.filterEndDate) {
      params.push(`startDate=${this.filterStartDate}T00:00:00`);
      params.push(`endDate=${this.filterEndDate}T23:59:59`);
    }

    url += params.join('&');

    this.http.get(url, {
      headers,
      responseType: 'blob'
    }).subscribe({
      next: (blob: Blob) => {
        const fileName = `stock-movements-${new Date().toISOString().split('T')[0]}.${format}`;
        const link = document.createElement('a');
        const url = window.URL.createObjectURL(blob);
        link.href = url;
        link.download = fileName;
        link.click();
        window.URL.revokeObjectURL(url);
        this.exporting = false;

        // Show success message briefly
        console.log(`${format.toUpperCase()} export completed successfully: ${fileName}`);
      },
      error: (error) => {
        console.error(`Error downloading ${format.toUpperCase()}:`, error);
        let errorMessage = `Failed to export ${format.toUpperCase()}.`;

        if (error.status === 401) {
          errorMessage = 'You need to be logged in to export data.';
        } else if (error.status === 500) {
          errorMessage = `Server error while generating ${format.toUpperCase()} export.`;
        } else if (error.status === 0) {
          errorMessage = 'Unable to connect to server. Please check your connection.';
        } else {
          errorMessage += ' Please try again.';
        }

        this.error = errorMessage;
        this.exporting = false;
      }
    });
  }

  navigateToOrder(orderId: number) {
    this.router.navigate(['/basic/orders', orderId]);
  }
}
