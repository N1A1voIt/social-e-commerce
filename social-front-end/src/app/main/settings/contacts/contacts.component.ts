import { Component } from '@angular/core';
import {NgIcon} from "@ng-icons/core";
import {ContectRowComponent} from "./contect-row/contect-row.component";

@Component({
  selector: 'app-contacts',
  standalone: true,
  imports: [
    NgIcon,
    ContectRowComponent
  ],
  templateUrl: './contacts.component.html',
  styleUrl: './contacts.component.css',
  providers: []
})
export class ContactsComponent {

}
