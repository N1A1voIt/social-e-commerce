import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { ProductCPL } from '../services/customer-product.service';

@Component({
  selector: 'app-shop-card',
  standalone: true,
  imports: [],
  templateUrl: './shop-card.component.html',
  styleUrls: ['./shop-card.component.css']
})
export class ShopCardComponent {
  @Input() product!: ProductCPL;
  @Input() productId!: number;

  constructor(private router: Router) {}

  goToDetail() {
    console.log(this.product)
    // Use the actual product ID from the product object if available
    const id = this.product?.idPc || this.productId;
    this.router.navigateByUrl('/client/marketplace/product/' + id);
  }
}
