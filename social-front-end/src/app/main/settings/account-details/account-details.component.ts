import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { ContactsComponent } from "../contacts/contacts.component";
import { ManagedAccountComponent } from "../managed-account/managed-account.component";
import { NgIcon } from "@ng-icons/core";
import { NgIf } from "@angular/common";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {environment, javaHost} from "../../../../environments/environment";
import {ProfileEditFormComponent} from "../profile-edit-form/profile-edit-form.component";


interface Seller {
  username: string;
  email: string;
  profileImage?: string;
}

@Component({
  selector: 'app-account-details',
  standalone: true,
  imports: [
    ContactsComponent,
    ManagedAccountComponent,
    NgIcon,
    NgIf,
    ProfileEditFormComponent
  ],
  templateUrl: './account-details.component.html',
  styleUrl: './account-details.component.css'
})
export class AccountDetailsComponent implements OnInit {
  formApply: string = 'jean';
  seller: Seller = {
    username: 'David Halloway',
    email: 'david@gmail.com',
    profileImage: 'assets/imgs/gustavo.jpeg'
  };
  isLoading: boolean = false;

  @Output() close = new EventEmitter<void>();

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadSellerInfo();
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

  closeDetails(): void {
    this.close.emit();
  }
}
