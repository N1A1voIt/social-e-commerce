import {Component, EventEmitter, Output} from '@angular/core';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent {
  @Output() addProductClicked = new EventEmitter<void>();

  onAddProduct() {
    this.addProductClicked.emit();
  }
}
