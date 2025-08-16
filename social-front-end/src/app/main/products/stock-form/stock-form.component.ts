import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {Product, Variant} from "../products.types";
import {NgForOf, NgIf} from "@angular/common";
import {StocksService} from "../stocks.service";
import {ApiResponse} from "../../inbox/inbox.service";

@Component({
  selector: 'app-stock-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgForOf,
    NgIf
  ],
  templateUrl: './stock-form.component.html',
  styleUrl: './stock-form.component.css'
})
export class StockFormComponent implements OnInit {
  stockForm!: FormGroup;
  loading = false;
  products: Product[] = [];
  variants: Variant[] = [];
  itemVariantOptions: Variant[][] = [];

  constructor(private fb: FormBuilder,private stockService:StocksService) {}
  onClose() {}

  ngOnInit(): void {
    this.stockForm = this.fb.group({
      description: ['', Validators.required],
      idOrderM: [null],
      createdAt: [new Date().toISOString()],
      items: this.fb.array([])
    });
    this.fetchUtils();
    this.addItem();
  }

  fetchUtils() {
    this.stockService.fetchStockUtilities().subscribe(
      {
        next: (data: ApiResponse) => {
          this.products = data.data.products;
          this.variants = data.data.variants;
        },
        error: (error) => {
          alert("Error fetching products: " + error.message);
        }
      }
    )
  }

  get items(): FormArray {
    return this.stockForm.get('items') as FormArray;
  }

  /**
   * Creates a new FormGroup for a stock item.
   * The 'price' field is now included but not shown in the UI.
   * Its value will be set programmatically.
   */
  createItem(): FormGroup {
    return this.fb.group({
      idProduct: [null, Validators.required],
      idVariant: [{ value: null, disabled: true }, Validators.required],
      price: [null, Validators.required], // Price is now set programmatically
      input: [0, [Validators.required, Validators.min(0)]],
      output: [0, [Validators.required, Validators.min(0)]],
      actionAt: [new Date().toISOString(), Validators.required],
    });
  }

  addItem(): void {
    this.items.push(this.createItem());
    this.itemVariantOptions.push([]);
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
    this.itemVariantOptions.splice(index, 1);
  }

  /**
   * Handles product selection. It filters available variants and sets the price
   * if the selected product does not have any variants.
   * @param itemIndex The index of the item row.
   */
  onProductSelect(itemIndex: number): void {
    const item = this.items.at(itemIndex) as FormGroup;
    const productId = item.get('idProduct')?.value;

    // Reset variant and price controls
    const variantControl = item.get('idVariant');
    const priceControl = item.get('price');
    variantControl?.reset();
    priceControl?.reset();
    this.itemVariantOptions[itemIndex] = [];

    if (productId) {
      const selectedProduct = this.products.find(p => p.idProduct.toString() === productId.toString());
      if (!selectedProduct) return;

      // Filter variants for the selected product
      const variantsForProduct = this.variants.filter(
        v => v.idProduct.toString() === productId.toString()
      );
      this.itemVariantOptions[itemIndex] = variantsForProduct;

      if (variantsForProduct.length > 0) {
        // If variants exist, enable the variant dropdown and wait for selection
        variantControl?.enable();
      } else {
        // If no variants, set price to the product's price and keep variant dropdown disabled
        priceControl?.setValue(selectedProduct.price);
        variantControl?.disable();
      }
    } else {
      variantControl?.disable();
    }
  }

  /**
   * Handles variant selection and sets the price control to the selected
   * variant's price.
   * @param itemIndex The index of the item row.
   */
  onVariantSelect(itemIndex: number): void {
    const item = this.items.at(itemIndex) as FormGroup;
    const variantId = item.get('idVariant')?.value;
    const priceControl = item.get('price');

    if (variantId) {
      const selectedVariant = this.itemVariantOptions[itemIndex].find(
        v => v.idVariant.toString() === variantId.toString()
      );
      if (selectedVariant) {
        priceControl?.setValue(selectedVariant.price);
      }
    } else {
      priceControl?.reset();
    }
  }

  onSubmit(): void {
    if (this.stockForm.invalid) {
      console.error('Form is invalid');
      this.stockForm.markAllAsTouched();
      return;
    }
    console.log('Form Submitted!');
    console.log(JSON.stringify(this.stockForm.value, null, 2));
    this.stockService.saveStocks(this.stockForm.value).subscribe({
      next: (data:ApiResponse) => {
        console.log('Stock created successfully:', data.data);
        alert("Stock created successfully");
      },
      error: (error:ApiResponse) => {
        console.error('Error creating stock:', error);
      }
    });
  }
}
