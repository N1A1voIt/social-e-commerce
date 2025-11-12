import { Component, OnInit } from '@angular/core';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { RippleModule } from 'primeng/ripple';
import { FormsModule } from '@angular/forms';
import {DecimalPipe, DatePipe, NgIf} from '@angular/common';
import { MessageService } from 'primeng/api';
import { BeautifulButtonComponent } from '../../shared/beautiful-button/beautiful-button.component';
import { SalesService } from './sales.service';
import { FrenchNumberPipe } from '../../shared/french-number.pipe';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-sales',
  standalone: true,
  providers: [MessageService],
  imports: [TableModule, ButtonModule, TagModule, ToastModule, RippleModule, FormsModule, DecimalPipe, DatePipe, BeautifulButtonComponent, NgIf, FrenchNumberPipe],
  templateUrl: './sales.component.html',
  styleUrls: ['./sales.component.css']
})
export class SalesComponent implements OnInit {
  sales: any[] = [];
  totalRecords = 0;
  loading = false;
  expandedRows: { [key: string]: boolean } = {};

  // Placeholder for selected sale / details
  activeSale: any = null;

  // CSV import state
  importLoading = false;

  constructor(private messagingService: MessageService, private salesService: SalesService) { }

  ngOnInit(): void {
    // initial load
    this.fetchSales({ first: 0, rows: 10 });
  }

  fetchSales(event?: TableLazyLoadEvent) {
    this.loading = true;
    const page = event && event.first !== undefined ? Math.floor((event.first / (event.rows || 10))) : 0;
    const size = event && event.rows ? event.rows : 10;
    const sort = event && (event as any).sortField ? `${(event as any).sortField},${(event as any).sortOrder === -1 ? 'desc' : 'asc'}` : undefined;

    this.salesService.fetchAllSales(page, size, sort).subscribe({
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

  onRowExpand(event: any) {
    // optionally load details for sale
  }

  onRowCollapse(event: any) {
    // no-op
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
          this.fetchSales({ first: 0, rows: 10 });
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
