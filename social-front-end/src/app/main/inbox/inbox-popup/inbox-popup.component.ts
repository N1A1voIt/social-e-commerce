import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormContainerComponent} from "../../../shared/form-container/form-container.component";
import {BeautifulButtonComponent} from "../../../shared/beautiful-button/beautiful-button.component";
import {VariantWithQuantity} from "../../products/products.types";
import {LoaderComponent} from "../../../shared/loader/loader.component";
import {DecimalPipe, NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-inbox-popup',
  standalone: true,
  imports: [
    FormContainerComponent,
    BeautifulButtonComponent,
    LoaderComponent,
    NgIf,
    NgForOf,
    DecimalPipe
  ],
  templateUrl: './inbox-popup.component.html',
  styleUrl: './inbox-popup.component.css'
})
export class InboxPopupComponent {
  @Input() isLoading = false;
  @Input() variants:VariantWithQuantity[] = [];
  @Output() closePopup = new EventEmitter<void>();
  getSubtotal(): number {
    return this.variants.reduce((total, variant) => total + variant.quantity * variant.variant.price, 0);
  }
}
