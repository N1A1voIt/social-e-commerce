import { Component, OnInit } from '@angular/core';
import {MvolaComponent} from "../transaction/mvola/mvola.component";
import {CommonModule, NgForOf, NgIf} from "@angular/common";
import {PaymentMethod} from "../transaction/transaction.component";
import {TransactionService} from "../transaction/transaction.service";
import {ActivatedRoute} from "@angular/router";
import {TempLink} from "../transaction/transaction.type";

@Component({
  selector: 'app-transaction-full',
  standalone: true,
  imports: [
    MvolaComponent,
    NgForOf,
    NgIf,
    CommonModule
  ],
  templateUrl: './transaction-full.component.html',
  styleUrl: './transaction-full.component.css'
})
export class TransactionFullComponent implements OnInit {
  selectedMethod: string = '';
  phoneNumber: string = '';
  amount: number = 0;
  tempLinkData: TempLink | null = null;
  paymentLinkId: string | null = null;
  isLoading: boolean = true;

  constructor(
    private transactionService: TransactionService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // Get the payment link ID from query params
    this.route.queryParams.subscribe(params => {
      this.paymentLinkId = params['id_payment'];
      if (this.paymentLinkId) {
        this.loadTempLinkData(this.paymentLinkId);
      }
    });
  }

  loadTempLinkData(linkId: string) {
    this.isLoading = true;
    this.transactionService.getTempLinkDetails(linkId).subscribe({
      next: (response) => {
        if (response.status === 200 && response.data) {
          this.tempLinkData = response.data;
          this.amount = this.tempLinkData?.amount || 0;
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading payment link data:', error);
        this.isLoading = false;
      }
    });
  }

  paymentMethods: PaymentMethod[] = [
    {
      id: 'mvola',
      name: 'Mvola',
      description: 'Pay with Mvola mobile money',
      icon: 'fas fa-mobile-alt',
      color: '#ffea06'
    },
    // {
    //   id: 'orange-money',
    //   name: 'Orange Money',
    //   description: 'Pay with Orange Money',
    //   icon: 'fas fa-credit-card',
    //   color: '#FF7900'
    // },
    // {
    //   id: 'airtel-money',
    //   name: 'Airtel Money',
    //   description: 'Pay with Airtel Money',
    //   icon: 'fas fa-wallet',
    //   color: '#ED1C24'
    // },
    // {
    //   id: 'bank-transfer',
    //   name: 'Bank Transfer',
    //   description: 'Direct bank transfer',
    //   icon: 'fas fa-university',
    //   color: '#28a745'
    // },
    // {
    //   id: 'credit-card',
    //   name: 'Credit/Debit Card',
    //   description: 'Pay with Visa, MasterCard, etc.',
    //   icon: 'fas fa-credit-card',
    //   color: '#6f42c1'
    // }
  ];

  selectPaymentMethod(methodId: string): void {
    this.selectedMethod = methodId;
  }

  getSelectedMethodName(): string {
    const method = this.paymentMethods.find(m => m.id === this.selectedMethod);
    return method ? method.name : '';
  }

  proceedWithPayment(): void {
    if (!this.selectedMethod) {
      alert('Please select a payment method');
      return;
    }

    if (!this.phoneNumber || !this.amount) {
      alert('Please fill in all required fields');
      return;
    }

    const selectedMethodName = this.getSelectedMethodName();
    const confirmMessage = `Proceed with ${selectedMethodName} payment?\n\nAmount: ${this.amount} MGA\nPhone: ${this.phoneNumber}`;

    if (confirm(confirmMessage)) {
      // Here you would typically call your payment service
      console.log('Payment initiated:', {
        method: this.selectedMethod,
        phone: this.phoneNumber,
        amount: this.amount
      });

      alert(`Payment request sent via ${selectedMethodName}! Check your phone for confirmation.`);
    }
  }

  cancel(): void {
    this.selectedMethod = '';
    this.phoneNumber = '';
    this.amount = 0;
  }
}
