import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Product} from "../products.types";
import {HttpClient} from "@angular/common/http";
import {javaHost} from "../../../../environments/environment";
import {NgForOf} from "@angular/common";

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
  products:Product[] = []
  constructor(private http: HttpClient) {
  }
  ngOnInit(): void {
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    this.http.get<Product[]>(javaHost+'/api/products?size=100&page=0',{headers:header}).subscribe(
      (data) => {
        this.products = data;
      },
      (error) => {
        console.error('Error fetching products:', error);
      }
    );
  }
  onAddProduct() {
    this.addProductClicked.emit();
  }
}
