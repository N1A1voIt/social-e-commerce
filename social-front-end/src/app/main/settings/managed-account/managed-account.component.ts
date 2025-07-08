import { Component } from '@angular/core';
import {NgIcon} from "@ng-icons/core";
import {PlatformRowComponent} from "./platform-row/platform-row.component";

@Component({
  selector: 'app-managed-account',
  standalone: true,
  imports: [
    NgIcon,
    PlatformRowComponent
  ],
  templateUrl: './managed-account.component.html',
  styleUrl: './managed-account.component.css'
})
export class ManagedAccountComponent {

}
