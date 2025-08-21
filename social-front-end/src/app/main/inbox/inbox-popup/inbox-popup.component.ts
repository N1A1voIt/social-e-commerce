import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormContainerComponent} from "../../../shared/form-container/form-container.component";
import {BeautifulButtonComponent} from "../../../shared/beautiful-button/beautiful-button.component";
import {OrderPreview, Product, Variant, VariantWithQuantity} from "../../products/products.types";
import {LoaderComponent} from "../../../shared/loader/loader.component";
import {DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {IPopupService} from "./i-popup.service";
import {ApiResponse} from "../inbox.service";

@Component({
  selector: 'app-inbox-popup',
  standalone: true,
  imports: [
    FormContainerComponent,
    BeautifulButtonComponent,
    LoaderComponent,
    NgIf,
    NgForOf,
    DecimalPipe,
    FormsModule
  ],
  templateUrl: './inbox-popup.component.html',
  styleUrl: './inbox-popup.component.css'
})
export class InboxPopupComponent implements OnInit {
  @Input() isLoading = false;
  @Input() showForm = false;
  @Input() variants!: OrderPreview;
  @Input() orderMessage: string = '';
  @Output() closePopup = new EventEmitter<void>();
  @Output() variantsUpdated = new EventEmitter<VariantWithQuantity[]>(); // Emit when variants are updated

  // Component state
  showUpdateMode = false;
  showCatalog = false;
  searchTerm = '';
  filteredProducts: Variant[] = [];

  constructor(private  iPopupService: IPopupService) { }

  ngOnInit() {
  }

  getSubtotal(): number {
    return this.variants?.variants.reduce((total, variant) => total + variant.quantity * variant.variant.price ,0);
  }

  toggleUpdateMode(event: Event): void {
    event.preventDefault();
    this.showUpdateMode = !this.showUpdateMode;
    if (!this.showUpdateMode) {
      this.showCatalog = false;
    }
  }

  saveOrder() {
    console.log(this.variants);
    this.iPopupService.saveOrder(this.variants).subscribe(
      {
        next: (response:ApiResponse) => {
          this.closePopup.emit();
        },error(err:ApiResponse) {
          alert(err.errors[0].message);
        }
      }
    )
  }

  toggleCatalog(): void {
    this.showCatalog = !this.showCatalog;
    if (this.showCatalog) {
      this.filterProducts(); // Initialize filtered products
    }
  }

  filterProducts(): void {
    this.iPopupService.fetchVariantBySKU(this.searchTerm).subscribe(
      {
        next: (response) => {
          this.filteredProducts = response.data;
        },error(err:ApiResponse) {
          alert(err.errors[0].message);
        }
      }
    )
  }

  increaseQuantity(index: number): void {
    if (index >= 0 && index < this.variants.variants.length) {
      this.variants.variants[index].quantity++;
      this.emitVariantsUpdate();
    }
  }

  decreaseQuantity(index: number): void {
    if (index >= 0 && index < this.variants.variants.length && this.variants.variants[index].quantity > 1) {
      this.variants.variants[index].quantity--;
      this.emitVariantsUpdate();
    }
  }

  removeItem(index: number): void {
    if (index >= 0 && index < this.variants.variants.length) {
      this.variants.variants.splice(index, 1);
      this.emitVariantsUpdate();
    }
  }

  addProductToOrder(product: Variant): void {
    const existingIndex = this.variants.variants.findIndex(v => v.variant.idVariant === product.idVariant);

    if (existingIndex !== -1) {
      this.variants.variants[existingIndex].quantity++;
    } else {
      const newVariant: VariantWithQuantity = {
        quantity: 1,
        variant: {
          idVariant: product.idProduct,
          title: product.title,
          price: product.price,
          mediaUrl: product.mediaUrl,
          idProduct: product.idProduct,
          createdAt: product.createdAt,
          updatedAt: product.updatedAt,
        }
      };
      this.variants.variants.push(newVariant);
    }

    this.emitVariantsUpdate();
  }

  saveChanges(): void {
    this.showUpdateMode = false;
    this.showCatalog = false;
    this.emitVariantsUpdate();
  }

  private emitVariantsUpdate(): void {
    this.variantsUpdated.emit([...this.variants.variants]);
  }
}
