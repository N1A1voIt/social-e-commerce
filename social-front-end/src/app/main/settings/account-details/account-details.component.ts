import { Component } from '@angular/core';
import {ContactsComponent} from "../contacts/contacts.component";
import {ManagedAccountComponent} from "../managed-account/managed-account.component";

@Component({
  selector: 'app-account-details',
  standalone: true,
  imports: [
    ContactsComponent,
    ManagedAccountComponent
  ],
  templateUrl: './account-details.component.html',
  styleUrl: './account-details.component.css'
})
export class AccountDetailsComponent {

}
