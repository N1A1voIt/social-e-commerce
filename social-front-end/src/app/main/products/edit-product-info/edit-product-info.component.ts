import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {javaHost} from '../../../../environments/environment';
import {ProductCpl, Category} from '../products.types';
import {SupabaseService} from '../../../shared/supabase.service';
import {AsyncPipe, NgForOf, NgIf} from '@angular/common';
import {BasicInputComponent} from '../../../shared/basic-input/basic-input.component';
import {BasicSelectComponent, SelectOption} from '../../../shared/basic-select/basic-select.component';
import {BasicButtonComponent} from '../../../shared/basic-button/basic-button.component';

@Component({
  selector: 'app-edit-product-info',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    NgForOf,
    AsyncPipe,
    BasicInputComponent,
    BasicSelectComponent,
    BasicButtonComponent
  ],
  template: `
    <form [formGroup]="productForm" (ngSubmit)="onSubmit()" class="space-y-4 w-full flex flex-col gap-1">
      <div class="mb-4">
        <h3 class="text-lg font-medium text-gray-900 mb-2">Edit Product Information</h3>
        <p class="text-sm text-gray-600">Update the basic details of your product.</p>
      </div>

      <input type="hidden" [formControl]="$any(productForm.get('idProduct'))">

      <app-basic-input
        class="w-full"
        label="Product Name"
        placeholder="Enter product name"
        [formControl]="$any(productForm.get('name'))"
        [error]="productForm.get('name')?.invalid && productForm.get('name')?.touched">
      </app-basic-input>

      <app-basic-input
        class="w-full"
        label="Description"
        placeholder="Enter product description"
        [formControl]="$any(productForm.get('description'))"
        type="textarea">
      </app-basic-input>

      <app-basic-input
        class="w-full"
        label="Price"
        placeholder="0.00"
        [formControl]="$any(productForm.get('price'))"
        type="number"
        [error]="productForm.get('price')?.invalid && productForm.get('price')?.touched">
      </app-basic-input>

      <app-basic-input
        class="w-full"
        label="SKU Prefix"
        placeholder="SKU"
        [formControl]="$any(productForm.get('sku_prefix'))"
        type="text"
        [error]="productForm.get('sku_prefix')?.invalid && productForm.get('sku_prefix')?.touched">
      </app-basic-input>

      <div class="w-full">
        <label class="block text-md font-semibold text-gray-700 mb-2">
          Product Image
        </label>

        <!-- File Input (Hidden) -->
        <input
          #fileInput
          type="file"
          accept="image/*"
          (change)="onFileSelected($event)"
          class="hidden">

        <!-- Custom File Picker Button -->
        <div class="border-8 border-dashed border-blue-50 rounded-lg p-6 text-center hover:border-gray-400 transition-colors">
          <div *ngIf="!selectedFile && !previewUrl" class="space-y-2">
            <svg class="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
              <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <div class="text-gray-600">
              <button type="button" (click)="fileInput.click()" class="text-blue-600 hover:text-blue-500 font-medium">
                Choose an image
              </button>
              <span class="text-gray-500"> or drag and drop</span>
            </div>
            <p class="text-xs text-gray-500">PNG, JPG, GIF up to 10MB</p>
          </div>

          <!-- File Preview -->
          <div *ngIf="selectedFile || previewUrl" class="space-y-4">
            <div class="relative inline-block">
              <img
                [src]="previewUrl || (productForm.get('media')?.value | async)"
                alt="Product preview"
                class="max-w-full max-h-48 rounded-lg shadow-md">

              <!-- Loading Overlay -->
              <div *ngIf="isUploadingFile"
                   class="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center rounded-lg">
                <div class="text-white text-center">
                  <svg class="animate-spin h-8 w-8 mx-auto mb-2" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <p class="text-sm">Uploading...</p>
                </div>
              </div>
            </div>

            <div class="flex justify-center space-x-2">
              <button
                type="button"
                (click)="fileInput.click()"
                [disabled]="isUploadingFile"
                class="px-3 py-1 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50">
                Change Image
              </button>
              <button
                type="button"
                (click)="removeFile()"
                [disabled]="isUploadingFile"
                class="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50">
                Remove
              </button>
            </div>
          </div>
        </div>

        <!-- Upload Error -->
        <div *ngIf="uploadError" class="mt-2 text-sm text-red-600">
          {{ uploadError }}
        </div>
      </div>

      <app-basic-select
        class="w-full"
        label="Category"
        placeholder="Type to search categories..."
        id="category-select"
        [options]="categoryOptions"
        [formControl]="$any(productForm.get('idCategory'))"
        [error]="productForm.get('idCategory')?.invalid && productForm.get('idCategory')?.touched"
        errorMessage="Please select a category">
      </app-basic-select>

      <div class="flex justify-between pt-4">
        <app-basic-button
          type="button"
          variant="secondary"
          (click)="onCancel()"
          [disabled]="isLoading">
          Cancel
        </app-basic-button>

        <app-basic-button
          type="submit"
          [disabled]="productForm.invalid || isLoading"
          [loading]="isLoading">
          Update Product
        </app-basic-button>
      </div>

      <!-- Error Message -->
      <div *ngIf="errorMessage" class="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg">
        <div class="flex">
          <svg class="w-5 h-5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          <div class="ml-3">
            <p class="text-sm text-red-600">{{errorMessage}}</p>
          </div>
        </div>
      </div>

      <!-- Success Message -->
      <div *ngIf="successMessage" class="mt-4 p-4 bg-green-50 border border-green-200 rounded-lg">
        <div class="flex">
          <svg class="w-5 h-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
          </svg>
          <div class="ml-3">
            <p class="text-sm text-green-600">{{successMessage}}</p>
          </div>
        </div>
      </div>
    </form>
  `,
  styleUrls: []
})
export class EditProductInfoComponent implements OnInit {
  @Input() product!: ProductCpl;
  @Output() formClosed = new EventEmitter<void>();
  @Output() productUpdated = new EventEmitter<ProductCpl>();

  productForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  categories: Category[] = [];
  categoryOptions: SelectOption[] = [];

  selectedFile: File | null = null;
  previewUrl: string | null = null;
  isUploadingFile = false;
  uploadError = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private supabaseService: SupabaseService
  ) {}

  ngOnInit() {
    console.log('EditProductInfoComponent ngOnInit called with product:', this.product);
    this.initializeForm();
    this.loadCategories();
    this.populateForm();
  }

  private initializeForm() {
    this.productForm = this.fb.group({
      idProduct: [null],
      name: ['', Validators.required],
      description: [''],
      price: ['', [Validators.required, Validators.min(0)]],
      media: [''],
      idSeller: [1],
      idCategory: [null, Validators.required],
      state: [true],
      sku_prefix: ['', Validators.required]
    });
  }

  private populateForm() {
    if (this.product) {
      this.productForm.patchValue({
        idProduct: this.product.idPc,
        name: this.product.name,
        description: this.product.description,
        price: this.product.price,
        media: this.product.media,
        idSeller: this.product.idSeller || 1,
        idCategory: this.product.idCategory || 1,
        state: true,
        sku_prefix: this.product.skuPrefix || ''
      });

      // Set preview URL for existing image
      if (this.product.media) {
        this.previewUrl = this.product.media;
      }
    }
  }

  private loadCategories() {
    this.http.get<any>(`${javaHost}/api/categories`)
      .subscribe({
        next: (response) => {
          this.categories = response.data || [];
          this.categoryOptions = this.categories.map(cat => ({
            value: cat.idCategory,
            label: cat.val,
            searchText: cat.val.toLowerCase()
          }));
        },
        error: (error) => {
          console.error('Error loading categories:', error);
          this.errorMessage = 'Failed to load categories';
        }
      });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.uploadError = '';

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.previewUrl = e.target?.result as string;
      };
      reader.readAsDataURL(file);

      // Upload to Supabase
      this.uploadToSupabase();
    }
  }

  private async uploadToSupabase() {
    if (!this.selectedFile) return;

    this.isUploadingFile = true;
    this.uploadError = '';

    try {
      const imageUrl = await this.supabaseService.uploadFile(this.selectedFile, 'products');
      this.productForm.patchValue({ media: imageUrl });

    } catch (error: any) {
      console.error('Error uploading file:', error);
      this.uploadError = 'Failed to upload image. Please try again.';
    } finally {
      this.isUploadingFile = false;
    }
  }

  removeFile() {
    this.selectedFile = null;
    this.previewUrl = null;
    this.productForm.patchValue({ media: '' });
    this.uploadError = '';
  }

  onSubmit() {
    if (this.productForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const productData = {
        name: this.productForm.value.name,
        description: this.productForm.value.description,
        price: this.productForm.value.price,
        media: this.productForm.value.media,
        idCategory: this.productForm.value.idCategory,
        skuPrefix: this.productForm.value.sku_prefix
      };

      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      });

      this.http.put(`${javaHost}/api/products/${this.product.idPc}`, productData, { headers })
        .subscribe({
          next: (response: any) => {
            this.successMessage = 'Product updated successfully!';
            this.isLoading = false;

            // Update the product object with new data
            const updatedProduct = { ...this.product, ...productData };
            this.productUpdated.emit(updatedProduct);

            // Close form after 1.5 seconds
            setTimeout(() => {
              this.onCancel();
            }, 1500);
          },
          error: (error) => {
            console.error('Error updating product:', error);
            this.errorMessage = error.error?.message || 'Failed to update product. Please try again.';
            this.isLoading = false;
          }
        });
    }
  }

  onCancel() {
    this.formClosed.emit();
  }
}
