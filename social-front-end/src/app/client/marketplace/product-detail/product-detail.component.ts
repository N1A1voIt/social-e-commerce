import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from "@angular/forms";
import { ActivatedRoute } from '@angular/router';
import { CustomerProductService, ProductCPL, ProductOptionDTO, OptionValueDTO, VariantInStock, SelectedOptionValues } from '../services/customer-product.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css'
})
export class ProductDetailComponent implements OnInit {
  productId!: number;
  product: ProductCPL | null = null;
  productOptions: ProductOptionDTO[] = [];
  selectedOptionValues: { [key: number]: number } = {}; // Map of optionId to selected optionValueId
  variant: VariantInStock | null = null;

  loading = {
    product: false,
    options: false,
    variant: false
  };

  error: string | null = null;

  // Fallback data for development
  fallbackProduct = {
    id_product: 1,
    name: 'Wireless Headphones',
    description: 'High-quality wireless headphones with noise cancellation. Enjoy up to 30 hours of playback, fast charging, and premium comfort. Perfect for travel and work.',
    price: 99.99,
    media: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=600&q=80',
    images: [
      'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=600&q=80',
      'https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=crop&w=600&q=80',
      'https://images.unsplash.com/photo-1526178613658-3f1622045557?auto=format&fit=crop&w=600&q=80',
      'https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=600&q=80'
    ],
    seller: {
      name: 'TechStore',
      rating: 4.8,
      sales: 1200
    }
  };

  selectedImage = this.fallbackProduct.media;
  quantity = 1;

  constructor(
    private route: ActivatedRoute,
    private productService: CustomerProductService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.productId = +params['id']; // Convert string to number
      this.fetchProductOptions();
    });
  }

  fetchProductOptions(): void {
    this.loading.options = true;
    this.error = null;

    this.productService.getProductOptions(this.productId)
      .subscribe({
        next: (options) => {
          this.productOptions = options;
          this.loading.options = false;

          // Initialize selected values with first option value for each option
          this.productOptions.forEach(option => {
            if (option.optionValues.length > 0) {
              this.selectedOptionValues[option.idOption] = option.optionValues[0].idOv;
            }
          });

          // Fetch variant based on initial selection
          this.fetchVariant();
        },
        error: (err) => {
          console.error('Error fetching product options:', err);
          this.error = 'Failed to load product options. Please try again later.';
          this.loading.options = false;

          // For development, create some sample options
          this.productOptions = [
            {
              idOption: 1,
              label: 'Color',
              optionValues: [
                { idOv: 1, value: 'Black' },
                { idOv: 2, value: 'White' },
                { idOv: 3, value: 'Red' }
              ]
            },
            {
              idOption: 2,
              label: 'Size',
              optionValues: [
                { idOv: 4, value: 'Small' },
                { idOv: 5, value: 'Medium' },
                { idOv: 6, value: 'Large' }
              ]
            }
          ];

          // Initialize selected values with first option value for each option
          this.productOptions.forEach(option => {
            if (option.optionValues.length > 0) {
              this.selectedOptionValues[option.idOption] = option.optionValues[0].idOv;
            }
          });
        }
      });
  }

  selectOptionValue(optionId: number, valueId: number): void {
    this.selectedOptionValues[optionId] = valueId;
    this.fetchVariant();
  }

  fetchVariant(): void {
    // Only proceed if we have selected values for all options
    if (this.productOptions.length > 0 && Object.keys(this.selectedOptionValues).length === this.productOptions.length) {
      this.loading.variant = true;

      const request: SelectedOptionValues = {
        productId: this.productId,
        optionValueIds: Object.values(this.selectedOptionValues)
      };

      this.productService.getVariantByOptionValues(request)
        .subscribe({
          next: (variant) => {
            this.variant = variant;
            this.loading.variant = false;
          },
          error: (err) => {
            console.error('Error fetching variant:', err);
            this.error = 'Failed to load variant. Please try again later.';
            this.loading.variant = false;

            // For development, create a sample variant
            this.variant = {
              idVariant: 1,
              title: 'Sample Variant',
              price: 99.99,
              idProduct: this.productId,
              createdAt: new Date().toISOString(),
              updatedAt: new Date().toISOString(),
              variantNumber: 10,
              stockStatus: 'In Stock'
            };
          }
        });
    }
  }

  selectImage(img: string) {
    this.selectedImage = img;
  }

  addToCart() {
    if (this.variant && this.variant.stockStatus !== 'Out of Stock') {
      alert(`Added ${this.quantity} of ${this.variant.title} to cart!`);
    } else {
      alert('This product is out of stock.');
    }
  }

  isOptionValueSelected(optionId: number, valueId: number): boolean {
    return this.selectedOptionValues[optionId] === valueId;
  }
}
