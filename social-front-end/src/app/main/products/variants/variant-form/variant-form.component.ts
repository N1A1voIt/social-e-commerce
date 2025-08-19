import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import {NgIf, NgForOf, CurrencyPipe, AsyncPipe} from '@angular/common';
import {
  ProductOption,
  ProductOptionValue,
  CreateVariantWithOptionsRequest,
  VariantWithOptionsDTO
} from '../../products.types';
import { VariantsService } from '../variants.service';
import { BasicInputComponent } from '../../../../shared/basic-input/basic-input.component';
import { BasicSelectComponent } from '../../../../shared/basic-select/basic-select.component';
import { BasicButtonComponent } from '../../../../shared/basic-button/basic-button.component';
import {ApiResponse} from "../../../inbox/inbox.service";
import {HttpErrorResponse} from "@angular/common/http";
import {SupabaseService} from "../../../../shared/supabase.service";

@Component({
  selector: 'app-variant-form',
  standalone: true,
  imports: [
    NgIf,
    NgForOf,
    ReactiveFormsModule,
    CurrencyPipe,
    BasicInputComponent,
    BasicSelectComponent,
    BasicButtonComponent,
    AsyncPipe
  ],
  templateUrl: './variant-form.component.html',
  styleUrl: './variant-form.component.css'
})
export class VariantFormComponent implements OnInit {
  @Input() productId!: number;
  @Input() isVisible: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() variantCreated = new EventEmitter<VariantWithOptionsDTO>();

  variantForm!: FormGroup;
  productOptions: ProductOption[] = [];
  loading: boolean = false;
  submitting: boolean = false;
  errorMessage: string = '';


  selectedFile: File | null = null;
  previewUrl: string | null = null;
  isUploadingFile = false;
  uploadError = '';

  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private variantsService: VariantsService,
    private supabaseService: SupabaseService
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    if (this.productId) {
      this.loadProductOptions();
    }
  }

  private initializeForm(): void {
    this.variantForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(2)]],
      price: [0, [Validators.required, Validators.min(0.01)]],
      sku: ['', [Validators.required, Validators.required]],
      media: [''],
      selectedOptions: this.fb.group({})
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      // Validate file type
      if (!file.type.startsWith('image/')) {
        this.uploadError = 'Please select an image file.';
        return;
      }

      // Validate file size (10MB max)
      if (file.size > 10 * 1024 * 1024) {
        this.uploadError = 'File size must be less than 10MB.';
        return;
      }

      this.selectedFile = file;
      this.uploadError = '';

      // Create preview URL
      const reader = new FileReader();
      reader.onload = (e) => {
        this.previewUrl = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  removeFile(): void {
    this.selectedFile = null;
    this.previewUrl = null;
    this.uploadError = '';

    // Clear the media URL from form if it was previously uploaded
    this.variantForm.patchValue({ media: '' });
  }


  private loadProductOptions(): void {
    this.loading = true;
    this.errorMessage = '';

    this.variantsService.fetchProductOptions(this.productId).subscribe({
      next: (options: ProductOption[]) => {
        this.productOptions = options;
        this.setupOptionControls();
        this.loading = false;
        // this.loadAllOptionValues();
      },
      error: (error) => {
        console.error('Error loading product options:', error);
        this.errorMessage = 'Failed to load product options';
        this.loading = false;
      }
    });
  }

  private setupOptionControls(): void {
    const selectedOptionsGroup = this.variantForm.get('selectedOptions') as FormGroup;

    this.productOptions.forEach(option => {
      selectedOptionsGroup.addControl(
        option.idOption.toString(),
        this.fb.control('', Validators.required)
      );
    });
  }


  getOptionSelectOptions(option:ProductOption): { value: any; label: string }[] {
    const values = option.optionValues;
    return values.map(value => ({
      value: value.idOv,
      label: value.value
    }));
  }

  // generateTitle(): void {
  //   const selectedOptionsGroup = this.variantForm.get('selectedOptions') as FormGroup;
  //   const selectedValues: string[] = [];
  //
  //   this.productOptions.forEach(option => {
  //     const selectedValueId = selectedOptionsGroup.get(option.idOption.toString())?.value;
  //     if (selectedValueId) {
  //       const optionValue = this.optionValues[option.idOption]?.find(v => v.idOv === selectedValueId);
  //       if (optionValue) {
  //         selectedValues.push(optionValue.value);
  //       }
  //     }
  //   });
  //
  //   if (selectedValues.length > 0) {
  //     const generatedTitle = selectedValues.join(' - ');
  //     this.variantForm.patchValue({ title: generatedTitle });
  //   }
  // }

  // onOptionChange(): void {
  //   // Auto-generate title when options change
  //   this.generateTitle();
  // }
  async uploadFileToSupabase(): Promise<string | null> {
    if (!this.selectedFile) {
      return null;
    }

    try {
      this.isUploadingFile = true;
      this.uploadError = '';

      const mediaUrl = await this.supabaseService.uploadFile(this.selectedFile, 'products');

      // Update form with the uploaded file URL
      this.variantForm.patchValue({ media: mediaUrl });

      return mediaUrl;
    } catch (error) {
      console.error('Upload error:', error);
      this.uploadError = 'Failed to upload image. Please try again.';
      return null;
    } finally {
      this.isUploadingFile = false;
    }
  }
  async onSubmit(): Promise<void> {
    if (this.variantForm.valid) {
      this.submitting = true;
      this.errorMessage = '';
      this.isLoading = true;
      if (this.selectedFile) {
        const uploadedUrl = await this.uploadFileToSupabase();
        if (!uploadedUrl) {
          this.isLoading = false;
          return; // Stop if upload failed
        }
        this.variantForm.patchValue({ media: uploadedUrl });
      }
      const formValue = this.variantForm.value;
      const selectedOptionsGroup = this.variantForm.get('selectedOptions') as FormGroup;

      // Collect selected option value IDs
      const optionValueIds: number[] = [];
      this.productOptions.forEach(option => {
        const selectedValueId = selectedOptionsGroup.get(option.idOption.toString())?.value;
        if (selectedValueId) {
          optionValueIds.push(selectedValueId);
        }
      });

      const request: CreateVariantWithOptionsRequest = {
        sku: formValue.sku,
        media: formValue.media,
        title: formValue.title,
        price: formValue.price,
        optionValueIds: optionValueIds
      };
      console.log('Creating variant with options:', request);
      this.variantsService.createVariantWithOptions(this.productId, request).subscribe({
        next: (variant: ApiResponse) => {
          this.submitting = false;
          const ret:VariantWithOptionsDTO = variant.data;
          this.variantCreated.emit(ret);
          this.onClose();
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error creating variant:', error);
          const err:ApiResponse = error.error;
          this.errorMessage = err.errors[0]?.message || 'Failed to create variant';
          this.submitting = false;
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.variantForm.controls).forEach(key => {
      const control = this.variantForm.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        Object.keys(control.controls).forEach(nestedKey => {
          control.get(nestedKey)?.markAsTouched();
        });
      }
    });
  }

  onClose(): void {
    this.variantForm.reset();
    this.errorMessage = '';
    this.close.emit();
  }

  getFieldError(fieldName: string): string {
    const field = this.variantForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} is required`;
      }
      if (field.errors['minlength']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
      if (field.errors['min']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} must be greater than ${field.errors['min'].min}`;
      }
    }
    return '';
  }

  getOptionError(optionId: number): string {
    const selectedOptionsGroup = this.variantForm.get('selectedOptions') as FormGroup;
    const control = selectedOptionsGroup.get(optionId.toString());

    if (control?.errors && control.touched) {
      if (control.errors['required']) {
        const option = this.productOptions.find(o => o.idOption === optionId);
        return `${option?.label || 'Option'} selection is required`;
      }
    }
    return '';
  }
}
