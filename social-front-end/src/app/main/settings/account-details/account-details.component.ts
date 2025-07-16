import { Component, Output, EventEmitter } from '@angular/core';
import {ContactsComponent} from "../contacts/contacts.component";
import {ManagedAccountComponent} from "../managed-account/managed-account.component";
import {NgIcon} from "@ng-icons/core";

@Component({
  selector: 'app-account-details',
  standalone: true,
  imports: [
    ContactsComponent,
    ManagedAccountComponent,
    NgIcon
  ],
  templateUrl: './account-details.component.html',
  styleUrl: './account-details.component.css'
})
export class AccountDetailsComponent {
  @Output() close = new EventEmitter<void>();

  closeDetails(): void {
    this.close.emit();
  }
}
