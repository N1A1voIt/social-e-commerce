import { Component, OnInit } from '@angular/core';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { RippleModule } from 'primeng/ripple';
import { FormsModule } from '@angular/forms';
import {DecimalPipe, DatePipe, NgIf, NgClass, NgForOf} from '@angular/common';
import { MessageService } from 'primeng/api';
import { BeautifulButtonComponent } from '../../shared/beautiful-button/beautiful-button.component';
import { SalesService } from './sales.service';
import { FrenchNumberPipe } from '../../shared/french-number.pipe';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { Router } from '@angular/router';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { PaginatorModule } from 'primeng/paginator';
import { BasicInputComponent } from '../../shared/basic-input/basic-input.component';
import { BasicButtonComponent } from '../../shared/basic-button/basic-button.component';

@Component({
  selector: 'app-sales',
  standalone: true,
  providers: [MessageService],
  imports: [TableModule, ButtonModule, TagModule, ToastModule, RippleModule, FormsModule, DecimalPipe, DatePipe, BeautifulButtonComponent, NgIf, NgClass, NgForOf, FrenchNumberPipe, DropdownModule, CalendarModule, BasicInputComponent, BasicButtonComponent, PaginatorModule],
  templateUrl: './sales.component.html',
  styleUrls: ['./sales.component.css']
})
export class SalesComponent implements OnInit {
  sales: any[] = [];
  totalRecords = 0;
  loading = false;
  expandedRows: { [key: string]: boolean } = {};

  // Pagination properties
  currentPage: number = 0;
  pageSize: number = 10;

  // Placeholder for selected sale / details
  activeSale: any = null;

  // CSV import state
  importLoading = false;
  
  // CSV export state
  exportLoading = false;

  // Filter properties
  filterStatus: number | null = null;
  filterFromName: string = '';
  filterOrderId: string = '';
  filterStartDate: Date | null = null;
  filterEndDate: Date | null = null;

  statusOptions = [
    { label: 'All', value: null },
    { label: 'Partially Paid', value: 1 },
    { label: 'Paid', value: 11 }
  ];

  constructor(
    private messagingService: MessageService,
    private salesService: SalesService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // initial load
    this.fetchSales();
  }

  toggleSaleDetails(sale: any, event?: Event): void {
    event?.stopPropagation();
    const saleId = sale.idSale?.toString() || '';
    
    if (this.expandedRows[saleId]) {
      // If already expanded, just collapse it
      delete this.expandedRows[saleId];
    } else {
      // If not expanded, expand it
      this.expandedRows[saleId] = true;
    }
  }

  isSaleExpanded(sale: any): boolean {
    const saleId = sale.idSale?.toString() || '';
    return !!this.expandedRows[saleId];
  }

  fetchSales(page?: number) {
    this.loading = true;
    const pageToFetch = page !== undefined ? page : this.currentPage;
    const size = this.pageSize;
    const sort = undefined;

    // Build filter parameters
    const filters: any = {};

    if (this.filterStatus !== null) {
      filters.status = this.filterStatus;
    }

    if (this.filterFromName && this.filterFromName.trim()) {
      filters.fromName = this.filterFromName.trim();
    }

    if (this.filterOrderId && this.filterOrderId.trim()) {
      filters.orderId = this.filterOrderId.trim();
    }

    let startDateStr = null;
    let endDateStr = null;

    if (this.filterStartDate) {
      // Set to start of day (00:00:00)
      const startDate = new Date(this.filterStartDate);
      startDate.setHours(0, 0, 0, 0);
      startDateStr = this.formatDateToISO(startDate);
    }

    if (this.filterEndDate) {
      // Set to end of day (23:59:59)
      const endDate = new Date(this.filterEndDate);
      endDate.setHours(23, 59, 59, 999);
      endDateStr = this.formatDateToISO(endDate);
    }

    if (startDateStr) {
      filters.startDate = startDateStr;
    }

    if (endDateStr) {
      filters.endDate = endDateStr;
    }

    this.salesService.fetchAllSales(pageToFetch, size, sort, filters).subscribe({
      next: (response) => {
        if (response && response.status === 200) {
          this.sales = response.data?.sales || [];
          console.log(this.sales)
          this.totalRecords = response.data?.totalSales || 0;
        } else {
          this.messagingService.add({ severity: 'warn', summary: 'Warning', detail: 'Failed to load sales' });
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching sales', err);
        this.messagingService.add({ severity: 'error', summary: 'Error', detail: 'Could not load sales' });
        this.loading = false;
      }
    });
  }

  onPageChange(event: any): void {
    this.currentPage = event.page;
    this.pageSize = event.rows;
    this.fetchSales(this.currentPage);
  }

  applyFilters() {
    this.currentPage = 0;
    this.fetchSales(0);
  }

  clearFilters() {
    this.filterStatus = null;
    this.filterFromName = '';
    this.filterStartDate = null;
    this.filterEndDate = null;
    this.filterOrderId = '';
    this.currentPage = 0;
    this.fetchSales(0);
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

  updateSaleStatus(sale: any) {
    this.salesService.payFull(sale.idSale).subscribe({
      next: (response) => {
        if (response && response.status === 200) {
          this.messagingService.add({ severity: 'success', summary: 'Success', detail: 'Sale status updated successfully' });
        } else {
          this.messagingService.add({ severity: 'warn', summary: 'Warning', detail: 'Failed to update sale status' });
        }
      },
      error: (err) => {
        console.error('Error updating sale status', err);
        this.messagingService.add({ severity: 'error', summary: 'Error', detail: 'Could not update sale status' });
      }
    });
  }

  async exportSale(sale: any) {
    // generate a simple PDF invoice using jsPDF
    const doc = new jsPDF();
    doc.setFontSize(18);
    doc.text('Invoice / Sale Details', 14, 22);

    doc.setFontSize(12);
    doc.text(`Sale ID: ${sale.idSale}`, 14, 34);
    doc.text(`Order ID: ${sale.idOrderM || 'N/A'}`, 14, 40);
    doc.text(`From: ${sale.fromName || sale.fromNumber || 'N/A'}`, 14, 46);
    doc.text(`Date: ${new Date(sale.effectuatedAt).toLocaleString()}`, 14, 52);
    doc.text(`Amount: ${sale.amount}`, 14, 58);

    const rows: any[] = [];
    // If sale has details array, add them to a table
    if (sale.details && Array.isArray(sale.details) && sale.details.length) {
      sale.details.forEach((d: any) => {
        rows.push([d.label || d.productName || '-', d.quantity || 1, d.price || 0, (d.price || 0) * (d.quantity || 1)]);
      });
    }

    if (rows.length) {
      autoTable(doc, {
        head: [['Item', 'Qty', 'Price', 'Total']],
        body: rows,
        startY: 70,
      });
      const finalY = (doc as any).lastAutoTable?.finalY || 70;
      doc.text(`Total: ${sale.amount}`, 14, finalY + 10);
    }

    doc.save(`Sale-${sale.idSale}.pdf`);

    this.messagingService.add({ severity: 'success', summary: 'Invoice', detail: `Invoice generated for sale #${sale.idSale}` });
  }

  viewSaleDetails(sale: any) {
    this.activeSale = sale;
    // open modal or route to details page
  }

  viewOrder(orderId: number, event?: Event) {
    event?.stopPropagation();
    // Navigate to order details page
    this.router.navigate(['/basic/orders', orderId]);
  }

  exportToCsv() {
    this.exportLoading = true;
    
    // Prepare query parameters for export
    const filters: any = {};

    
    // Add filters if they exist
    if (this.filterStatus !== null) {
      filters.status = this.filterStatus;
    }
    if (this.filterFromName?.trim()) {
      filters.fromName = this.filterFromName.trim();
    }
    if (this.filterOrderId?.trim()) {
      filters.orderId = this.filterOrderId.trim();
    }
    if (this.filterStartDate) {
      const startDate = new Date(this.filterStartDate);
      startDate.setHours(0, 0, 0, 0);
      filters.startDate = this.formatDateToISO(startDate);
    }
    if (this.filterEndDate) {
      const endDate = new Date(this.filterEndDate);
      endDate.setHours(23, 59, 59, 999);
      filters.endDate = this.formatDateToISO(endDate);
    }
    
    console.log('Export filters:', filters);
    console.log('Current sales in table:', this.sales.length);
    
    // If no filters are applied, try to export what's currently displayed
    if (Object.keys(filters).length === 0 && this.sales.length > 0) {
      console.log('No filters applied, exporting current table data');
      this.generateCsv(this.sales);
      this.exportLoading = false;
      return;
    }
    
    // Try using backend CSV export first, fallback to frontend generation
    this.salesService.exportCsv(filters).subscribe({
      next: (blob: Blob) => {
        // Create download link for the CSV blob
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `sales-export-${new Date().toISOString().split('T')[0]}.csv`;
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
        
        this.messagingService.add({
          severity: 'success',
          summary: 'Export Complete',
          detail: 'Sales data exported successfully'
        });
        this.exportLoading = false;
      },
      error: (error: any) => {
        console.error('Backend CSV export failed, trying frontend generation:', error);
        // Fallback to frontend CSV generation
        this.fallbackCsvExport(filters);
      }
    });
  }
  
  fallbackCsvExport(filters: any) {
    console.log('Using fallback CSV export with filters:', filters);
    this.salesService.fetchAllSales(0, 99999, undefined, filters).subscribe({
      next: (response: any) => {
        console.log('Fallback export response:', response);
        if (response && response.status === 200) {
          const salesData = response.data?.sales || [];
          console.log('Sales data for export:', salesData.length, 'records');
          this.generateCsv(salesData);
        } else {
          this.messagingService.add({
            severity: 'error',
            summary: 'Export Failed',
            detail: 'Unable to export sales data'
          });
        }
        this.exportLoading = false;
      },
      error: (error: any) => {
        console.error('Error exporting sales:', error);
        this.messagingService.add({
          severity: 'error',
          summary: 'Export Failed',
          detail: 'An error occurred while exporting sales data'
        });
        this.exportLoading = false;
      }
    });
  }
  
  generateCsv(salesData: any[]) {
    if (!salesData || salesData.length === 0) {
      this.messagingService.add({
        severity: 'warn',
        summary: 'No Data',
        detail: 'No sales data available to export'
      });
      return;
    }
    
    // CSV headers
    const headers = [
      'Sale ID',
      'Description', 
      'From Name',
      'From Number',
      'Amount (Ar)',
      'Paid Amount (Ar)',
      'Status',
      'Order ID',
      'Date',
      'Products'
    ];
    
    // Convert data to CSV format
    const csvData = salesData.map(sale => {
      const products = sale.details?.map((detail: any) => 
        `${detail.productName || detail.label || 'N/A'} (${detail.variantName || 'N/A'}) x${detail.quantity || detail.qty || 0}`
      ).join('; ') || 'No products';
      
      return [
        sale.idSale || '',
        (sale.description || '').replace(/"/g, '""'), // Escape quotes
        (sale.fromName || '').replace(/"/g, '""'),
        sale.fromNumber || '',
        sale.amount || 0,
        sale.paidAmount || 0,
        sale.status === 1 ? 'Partially Paid' : 'Paid',
        sale.idOrderM || '',
        sale.effectuatedAt ? new Date(sale.effectuatedAt).toLocaleString() : '',
        products.replace(/"/g, '""')
      ];
    });
    
    // Create CSV content
    const csvContent = [
      headers.join(','),
      ...csvData.map(row => 
        row.map(cell => 
          typeof cell === 'string' && (cell.includes(',') || cell.includes('"') || cell.includes('\n')) 
            ? `"${cell}"` 
            : cell
        ).join(',')
      )
    ].join('\n');
    
    // Download CSV
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.href = url;
    link.download = `sales-export-${new Date().toISOString().split('T')[0]}.csv`;
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    this.messagingService.add({
      severity: 'success',
      summary: 'Export Complete',
      detail: `Exported ${salesData.length} sales records to CSV`
    });
  }

  onImportCsvClick() {
    // Trigger file input click
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = '.csv';
    fileInput.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        this.importCsv(file);
      }
    };
    fileInput.click();
  }

  importCsv(file: File) {
    if (!file.name.toLowerCase().endsWith('.csv')) {
      this.messagingService.add({
        severity: 'error',
        summary: 'Invalid File',
        detail: 'Please select a CSV file'
      });
      return;
    }

    this.importLoading = true;
    this.salesService.importCsv(file).subscribe({
      next: (response) => {
        if (response && response.status === 200) {
          this.messagingService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'CSV imported successfully'
          });
          // Refresh the sales list after import
          this.currentPage = 0;
          this.fetchSales(0);
        } else {
          this.messagingService.add({
            severity: 'warn',
            summary: 'Warning',
            detail: 'Failed to import CSV'
          });
        }
        this.importLoading = false;
      },
      error: (err) => {
        console.error('Error importing CSV', err);
        this.messagingService.add({
          severity: 'error',
          summary: 'Error',
          detail: err.error?.errors?.[0] || 'Could not import CSV file'
        });
        this.importLoading = false;
      }
    });
  }
}
