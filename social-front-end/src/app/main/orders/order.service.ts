import { Injectable } from '@angular/core';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import {HttpClient} from "@angular/common/http";
import {ApiResponse} from "../inbox/inbox.service";
import {javaHost} from "../../../environments/environment";
import {Observable} from "rxjs";
import {DeliveryMission, OrderChild, OrderParent} from "./order.type";


@Injectable({
  providedIn: 'root'
})
export class OrderService {

  constructor(private http:HttpClient) { }

  fetchAllOrders(pageNum:number):Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ApiResponse>(javaHost+'/api/orders?size=10&page='+pageNum,{headers:header});
  }

  sendBillingAndPaymentLink(order:OrderParent):Observable<ApiResponse> {
    const  header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    }
    return this.http.post<ApiResponse>(javaHost+'/api/order/ask-for-pay',order,{headers:header});
  }

  fetchOrderChild(id:number):Observable<ApiResponse> {
    console.log('Fetching child orders for order ID:', id);
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    return this.http.get<ApiResponse>(javaHost+'/api/orders/'+id,{headers:header});
  }
  async generateOrderPdf(order: OrderParent) {
    const doc = new jsPDF();

    if (!order.childs || order.childs.length === 0) {
      try {
        const response = await this.fetchOrderChild(order.idOrderM!).toPromise();
        order.childs = response?.data?.childs || [];
      } catch (error) {
        console.error('Failed to fetch child items', error);
        order.childs = [];
      }
    }

    doc.setFontSize(18);
    doc.text('Invoice / Order Details', 14, 22);

    doc.setFontSize(12);
    doc.text(`Order ID: ${order.idOrderM}`, 14, 32);
    doc.text(`Customer: ${order.dcustomerName}`, 14, 40);
    doc.text(`Phone: ${order.customerNumber}`, 14, 48);
    doc.text(`Shipping Address: ${order.shippingAddress}`, 14, 56);
    doc.text(`Created At: ${order.createdAt}`, 14, 64);

    const tableColumn = ['SKU', 'Product', 'Quantity', 'Price', 'Total'];
    const tableRows: any[] = [];

    order.childs?.forEach((child: OrderChild) => {
      tableRows.push([
        child.sku || '',
        child.productName || '',
        child.quantity || 0,
        child.price || 0,
        (child.price || 0) * (child.quantity || 0)
      ]);
    });

    // ✅ use autoTable correctly
    autoTable(doc, {
      head: [tableColumn],
      body: tableRows,
      startY: 70,
    });

    const finalY = (doc as any).lastAutoTable?.finalY || 70;
    doc.text(`Total: $${order.dtotal}`, 14, finalY + 10);

    doc.save(`Order-${order.idOrderM}.pdf`);
  }

  sendMission(order:DeliveryMission):Observable<ApiResponse> {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    }
    return this.http.post<ApiResponse>(javaHost+'/api/order/call-for-tenders',order,{headers:header});
  }
  fetchApplicants(idOrder:number) {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    }
    return this.http.get<ApiResponse>(javaHost+'/api/applications/'+idOrder, {headers:header});
  }
}
