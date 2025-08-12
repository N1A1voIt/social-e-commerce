import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { NgIf, NgForOf, CurrencyPipe } from '@angular/common';
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
    BasicButtonComponent
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

  constructor(
    private fb: FormBuilder,
    private variantsService: VariantsService
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
      selectedOptions: this.fb.group({})
    });
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

  onSubmit(): void {
    if (this.variantForm.valid) {
      this.submitting = true;
      this.errorMessage = '';

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
        title: formValue.title,
        price: formValue.price,
        optionValueIds: optionValueIds
      };

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
