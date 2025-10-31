import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import {NgIcon} from "@ng-icons/core";
import {ContectRowComponent} from "./contect-row/contect-row.component";
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { javaHost } from '../../../../environments/environment';
import {NgForOf, NgIf} from "@angular/common";

/**
 * Contacts list: loads seller phone numbers and emits `editPhoneNumber` when a row requests editing.
 */
@Component({
  selector: 'app-contacts',
  standalone: true,
  imports: [
    NgIcon,
    ContectRowComponent,
    NgIf,
    NgForOf
  ],
  templateUrl: './contacts.component.html',
  styleUrl: './contacts.component.css',
  providers: []
})
export class ContactsComponent implements OnInit {
  phoneNumbers: any[] = [];
  isLoading = false;

  @Output() editPhoneNumber = new EventEmitter<any>();

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadPhoneNumbers();
  }

  loadPhoneNumbers() {
    this.isLoading = true;
    const token = localStorage.getItem('token') || '';
    const headers = new HttpHeaders({ 'Authorization': token });
    this.http.get(`${javaHost}/api/sellers/phone-numbers`, { headers }).subscribe({
      next: (res: any) => {
        if (res && res.status === 200 && Array.isArray(res.data)) {
          this.phoneNumbers = res.data;
        } else if (Array.isArray(res)) {
          // fallback if backend returns raw array
          this.phoneNumbers = res;
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load phone numbers', err);
        this.isLoading = false;
      }
    });
  }

  onEdit(phone: any) {
    this.editPhoneNumber.emit(phone);
  }
}
