import { Component } from '@angular/core';
import {ContactsComponent} from "../../main/settings/contacts/contacts.component";
import {ManagedAccountComponent} from "../../main/settings/managed-account/managed-account.component";
import {NgIcon} from "@ng-icons/core";
import {LoginInputComponent} from "../forms/auth/login-input/login-input.component";
import {BasicInputComponent} from "../basic-input/basic-input.component";
import {BasicButtonComponent} from "../basic-button/basic-button.component";

@Component({
  selector: 'app-form-container',
  standalone: true,
  imports: [
    ContactsComponent,
    ManagedAccountComponent,
    NgIcon,
    LoginInputComponent,
    BasicInputComponent,
    BasicButtonComponent
  ],
  templateUrl: './form-container.component.html',
  styleUrl: './form-container.component.css'
})
export class FormContainerComponent {

}
