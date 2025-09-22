import { Component } from '@angular/core';
import {FormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {MvolaComponent} from "./mvola/mvola.component";

@Component({
  selector: 'app-transaction',
  standalone: true,
  imports: [
    FormsModule,
    NgForOf,
    NgIf,
    MvolaComponent
  ],
  templateUrl: './transaction.component.html',
  styleUrl: './transaction.component.css'
})
export class TransactionComponent {
  selectedMethod: string = '';
  phoneNumber: string = '';
  amount: number = 0;

  paymentMethods: PaymentMethod[] = [
    {
      id: 'mvola',
      name: 'Mvola',
      description: 'Pay with Mvola mobile money',
      icon: 'fas fa-mobile-alt',
      color: '#ffea06'
    },
    {
      id: 'orange-money',
      name: 'Orange Money',
      description: 'Pay with Orange Money',
      icon: 'fas fa-credit-card',
      color: '#FF7900'
    },
    {
      id: 'airtel-money',
      name: 'Airtel Money',
      description: 'Pay with Airtel Money',
      icon: 'fas fa-wallet',
      color: '#ED1C24'
    },
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


interface PaymentMethod {
  id: string;
  name: string;
  description: string;
  icon: string;
  color: string;
}

