import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Product, ProductCpl} from "../products.types";
import {HttpClient} from "@angular/common/http";
import {javaHost} from "../../../../environments/environment";
import {NgForOf} from "@angular/common";
import {ProductServiceService} from "../product-service.service";

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {
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
}
