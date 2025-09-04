import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { NgIcon } from "@ng-icons/core";
import { PlatformRowComponent } from "./platform-row/platform-row.component";
import {ManagedPageCPL} from "../account-details/account-details.component";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-managed-account',
  standalone: true,
  imports: [
    NgIcon,
    PlatformRowComponent,
    NgForOf
  ],
  templateUrl: './managed-account.component.html',
  styleUrl: './managed-account.component.css'
})
export class ManagedAccountComponent {
   @Input() managedPages:ManagedPageCPL[] = [];
   @Output() editClicked = new EventEmitter<void>();
   @Output() addShippingPoint = new EventEmitter<number>();

  triggerEdit(): void {
    this.editClicked.emit();
  }

  onAddShippingPoint(managedPageId: number): void {
    this.addShippingPoint.emit(managedPageId);
  }
}
