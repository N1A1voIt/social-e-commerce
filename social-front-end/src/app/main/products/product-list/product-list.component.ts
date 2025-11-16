import {Component, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import {Product, ProductCpl} from "../products.types";
import {HttpClient} from "@angular/common/http";
import {javaHost} from "../../../../environments/environment";
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {ProductServiceService} from "../product-service.service";
import {FrenchNumberPipe} from "../../../shared/french-number.pipe";

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    NgForOf,
    NgClass,
    FrenchNumberPipe,
    NgIf
  ],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {
  openMenuProductId: number | null = null;
  @Output() addProductClicked = new EventEmitter<void>();
  @Output() editProductClicked = new EventEmitter<ProductCpl>();
  @Input() productsList: ProductCpl[] = [];
  constructor(private http: HttpClient,private productService:ProductServiceService) {
  }
  ngOnInit(): void {
  }
  @Output() navigateToVariants:EventEmitter<number> = new EventEmitter<number>();
  onAddProduct() {
    this.addProductClicked.emit();
  }

  onEditProduct(product: ProductCpl, event: Event) {
    event.stopPropagation();
    this.editProductClicked.emit(product);
  }

  toggleMenu(productId: number, event: Event) {
    event.stopPropagation();
    this.openMenuProductId = this.openMenuProductId === productId ? null : productId;
  }

  isMenuOpen(productId: number): boolean {
    return this.openMenuProductId === productId;
  }

  onDeleteProduct(product: ProductCpl, event: Event) {
    event.stopPropagation();
    // Implement delete logic here
    console.log('Delete product:', product);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    // Close menu when clicking outside
    this.openMenuProductId = null;
  }
}
