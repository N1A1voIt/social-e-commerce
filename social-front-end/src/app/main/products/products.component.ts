import { Component } from '@angular/core';
import {ProductListComponent} from "./product-list/product-list.component";
import {FormContainerComponent} from "../../shared/form-container/form-container.component";

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [
    ProductListComponent,
    FormContainerComponent
  ],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css'
})
export class ProductsComponent {

}
