import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-shop-card',
  standalone: true,
  imports: [],
  templateUrl: './shop-card.component.html',
  styleUrls: ['./shop-card.component.css']
})
export class ShopCardComponent {
  @Input() product: any;
  @Input() productId!: number;

  constructor(private router: Router) {}

  goToDetail() {
    this.router.navigateByUrl('/client/marketplace/product/'+this.productId);
  }
}
