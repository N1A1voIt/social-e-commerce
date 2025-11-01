import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {environment, javaHost} from '../../../../environments/environment';
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
  constructor(private http: HttpClient) {}
   @Input() managedPages:ManagedPageCPL[] = [];
   @Output() editClicked = new EventEmitter<void>();
   @Output() addShippingPoint = new EventEmitter<number>();
   @Output() addAmountDistance = new EventEmitter<number>();
   @Output() fetchNumbersForApage = new EventEmitter<number>();
  triggerEdit(): void {
    this.editClicked.emit();
  }

  onAddShippingPoint(managedPageId: number): void {
    this.addShippingPoint.emit(managedPageId);
  }

  onAddAmountDistance(managedPageId: number): void {
    this.addAmountDistance.emit(managedPageId);
  }
  onFetchNumbersForApage(managedPageId: number): void {
    this.fetchNumbersForApage.emit(managedPageId);
  }

  onReconnect(platform: string): void {
    const token = localStorage.getItem("token");
    if (!token) {
      console.error("No token found");
      return;
    }

    // const headers = new HttpHeaders().set("Authorization", token.replace("Bearer ", ""));

    // Redirect to the reconnect endpoint
    window.location.href = `${javaHost}'/api/auth/${platform}/callback`;
  }

  protected readonly javaHost = javaHost;
}

