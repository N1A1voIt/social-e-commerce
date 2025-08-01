import {Component, Input} from '@angular/core';
import {Product} from "../../products/products.types";

@Component({
  selector: 'app-product-row',
  standalone: true,
  imports: [],
  templateUrl: './product-row.component.html',
  styleUrl: './product-row.component.css'
})
export class ProductRowComponent {
  @Input() product!: Product;
}
