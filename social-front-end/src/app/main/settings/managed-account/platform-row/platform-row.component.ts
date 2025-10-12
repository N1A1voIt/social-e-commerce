import {Component, Input, Output, EventEmitter} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {environment, javaHost} from '../../../../../environments/environment';
import {CheckboxComponent} from "../../../../shared/checkbox/checkbox.component";
import {ShippingPointFormComponent} from "../shipping-point/shipping-point-form.component";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-platform-row',
  standalone: true,
  imports: [
    CheckboxComponent,
    ShippingPointFormComponent,
    NgIf
  ],
  templateUrl: './platform-row.component.html',
  styleUrl: './platform-row.component.css'
})
export class PlatformRowComponent {
  constructor(private http: HttpClient) {}
  @Input() platform!: string;
  @Input() pageTitle!: string;
  @Input() username!: string;
  @Input() logo!: string;  // Use to dynamically set the image
  @Input() status!: string;
  @Input() associatedMedia!: string;
  @Input() linkToPlatform!: string;
  @Input() managedPageId!: number;
  @Input() refreshToken?: string;
  @Input() accessToken?: string;
  @Output() reconnectClicked = new EventEmitter<string>();

  showShippingPointForm = false;

  onAddShippingPoint(): void {
    this.showShippingPointForm = true;
  }

  closeShippingPointForm = (): void => {
    this.showShippingPointForm = false;
  }
  get isConnected(): boolean {
    return !!(this.refreshToken && this.accessToken);
  }

  onReconnect(): void {
    this.reconnectClicked.emit(this.platform);
  }
}

