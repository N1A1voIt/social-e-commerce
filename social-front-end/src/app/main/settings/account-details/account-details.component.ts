import {Component, Output, EventEmitter, OnInit, ChangeDetectorRef} from '@angular/core';
import { ContactsComponent } from "../contacts/contacts.component";
import { ManagedAccountComponent } from "../managed-account/managed-account.component";
import { NgIcon } from "@ng-icons/core";
import { NgIf } from "@angular/common";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { javaHost } from "../../../../environments/environment";
import {ProfileEditFormComponent} from "../profile-edit-form/profile-edit-form.component";
import {ShippingPointFormComponent} from "../managed-account/shipping-point/shipping-point-form.component";
import {AmountDistanceFormComponent} from "../managed-account/amount-distance/amount-distance-form.component";
import { FormsModule } from '@angular/forms';

export interface ManagedPageCPL {
  idMp: number;
  status: string;
  platformIdentifier: string;
  pageTitle: string;
  associatedMedia: string;
  linkToPlatform: string;
  platform: string;
  email: string;
  idSeller: number;
  username: string;
}

interface Seller {
  username: string;
  email: string;
  profileImage?: string;
}

// New interface for phone number response
export interface SellerPhoneNumberResponse {
  id?: number;
  phoneNumber: string;
  associatedName: string;
  paymentMethodId?: number | null;
  paymentMethodName?: string;
  sellerId?: number;
}

@Component({
  selector: 'app-account-details',
  standalone: true,
  imports: [
    ContactsComponent,
    ManagedAccountComponent,
    NgIcon,
    NgIf,
    ProfileEditFormComponent,
    ShippingPointFormComponent,
    AmountDistanceFormComponent,
    FormsModule
  ],
  templateUrl: './account-details.component.html',
  styleUrls: ['./account-details.component.css']
})
export class AccountDetailsComponent implements OnInit {
  formApply: string = 'jean';
  seller: Seller = {
    username: 'David Halloway',
    email: 'david@gmail.com',
    profileImage: 'assets/imgs/gustavo.jpeg'
  };
  facebookUrl = javaHost + '/api/auth/facebook/login';
  instagramUrl = javaHost + '/api/auth/instagram/login';
  onEditClicked() {
    this.formApply = 'accounts';
  }
  isLoading: boolean = false;
  managedPages: ManagedPageCPL[] = [];
  selectedManagedPageId: number | null = null;
  @Output() close = new EventEmitter<void>();

  // Selected phone number for editing
  selectedPhoneNumber: SellerPhoneNumberResponse | null = null;

  onAddShippingPoint(managedPageId: number): void {
    this.selectedManagedPageId = managedPageId;
    this.formApply = 'shipping-points';
  }

  onAddAmountDistance(managedPageId: number): void {
    this.selectedManagedPageId = managedPageId;
    this.formApply = 'amount-distance';
  }

  closeShippingPointForm(): void {
    this.formApply = 'jean';
    this.selectedManagedPageId = null;
  }

  closeAmountDistanceForm(): void {
    this.formApply = 'jean';
    this.selectedManagedPageId = null;
  }

  constructor(private http: HttpClient,private cdr:ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadSellerInfo();
    this.loadManagedPages();
  }

  // Handler called by app-contacts when user clicks edit
  onEditPhoneNumber(phoneNumber: SellerPhoneNumberResponse) {
    this.selectedPhoneNumber = { ...phoneNumber };
    this.formApply = 'contacts';
  }

  // Open form to create a new phone number
  openNewPhoneNumber() {
    this.selectedPhoneNumber = {
      phoneNumber: '',
      associatedName: '',
      paymentMethodId: null
    };
    this.formApply = 'contacts';
  }

  // Save (create or update) phone number
  savePhoneNumber(): void {
    if (!this.selectedPhoneNumber) return;
    const token = localStorage.getItem('token') || '';
    const authHeader = token.startsWith('Bearer') ? token : `Bearer ${token}`;
    const headers = new HttpHeaders({ 'Authorization': authHeader, 'Content-Type': 'application/json' });

    const payload = {
      phoneNumber: this.selectedPhoneNumber.phoneNumber,
      associatedName: this.selectedPhoneNumber.associatedName,
      paymentMethodId: this.selectedPhoneNumber.paymentMethodId
    };

    if (this.selectedPhoneNumber.id) {
      // update
      this.http.put(`${javaHost}/api/sellers/phone-numbers/${this.selectedPhoneNumber.id}`, payload, { headers })
        .subscribe({
          next: (_res: any) => {
            this.formApply = 'jean';
            this.selectedPhoneNumber = null;
          },
          error: (err) => {
            console.error('Failed to update phone number', err);
          }
        });
    } else {
      // create
      this.http.post(`${javaHost}/api/sellers/phone-numbers`, payload, { headers })
        .subscribe({
          next: (_res: any) => {
            this.formApply = 'jean';
            this.selectedPhoneNumber = null;
          },
          error: (err) => {
            console.error('Failed to create phone number', err);
          }
        });
    }
  }

  deletePhoneNumber(): void {
    if (!this.selectedPhoneNumber?.id) return;
    const token = localStorage.getItem('token') || '';
    const authHeader = token.startsWith('Bearer') ? token : `Bearer ${token}`;
    const headers = new HttpHeaders({ 'Authorization': authHeader });
    this.http.delete(`${javaHost}/api/sellers/phone-numbers/${this.selectedPhoneNumber.id}`, { headers })
      .subscribe({
        next: () => {
          this.formApply = 'jean';
          this.selectedPhoneNumber = null;
        },
        error: (err) => {
          console.error('Failed to delete phone number', err);
        }
      });
  }

  private loadSellerInfo(): void {
    this.isLoading = true;
    const token = localStorage.getItem('token');

    if (!token) {
      this.isLoading = false;
      return;
    }

    const headers = new HttpHeaders({
      'Authorization': token
    });

    this.http.get(`${javaHost}/api/auth/me`, { headers })
      .subscribe({
        next: (response: any) => {
          if (response.status === 200 && response.data) {
            this.seller = {
              username: response.data.username,
              email: response.data.email,
              profileImage: this.seller.profileImage // Keep default image if none provided
            };
          }
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
        }
      });
  }
  private loadManagedPages(): void {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');
    this.http.get<ManagedPageCPL[]>(javaHost + '/api/auth/managed-pages-all', { headers }).subscribe({
      next: (data) => {
        this.managedPages = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.log(err.message);
        console.error('Failed to load managed pages', err);
      },
    });
  }

  closeDetails(): void {
    this.close.emit();
  }
}
