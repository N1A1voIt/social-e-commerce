import {Component, Output, EventEmitter, OnInit, ChangeDetectorRef} from '@angular/core';
import { ContactsComponent } from "../contacts/contacts.component";
import { ManagedAccountComponent } from "../managed-account/managed-account.component";
import { NgIcon } from "@ng-icons/core";
import { NgIf } from "@angular/common";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {environment, javaHost} from "../../../../environments/environment";
import {ProfileEditFormComponent} from "../profile-edit-form/profile-edit-form.component";

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
  facebookUrl = javaHost + '/api/auth/facebook/login';
  instagramUrl = javaHost + '/api/auth/instagram/login';
  onEditClicked() {
    this.formApply = 'accounts';
  }
  isLoading: boolean = false;
  managedPages: ManagedPageCPL[] = [];
  @Output() close = new EventEmitter<void>();

  constructor(private http: HttpClient,private cdr:ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadSellerInfo();
    this.loadManagedPages();
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
