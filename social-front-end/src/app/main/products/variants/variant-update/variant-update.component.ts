import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {UpdateVariantRequest, VariantWithOptionsDTO} from "../../products.types";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {VariantsService} from "../variants.service";
import {BasicInputComponent} from "../../../../shared/basic-input/basic-input.component";
import {ImportsModule} from "../../../../import/import.module";
import {NgIf} from "@angular/common";
import {BasicButtonComponent} from "../../../../shared/basic-button/basic-button.component";
import {FormContainerComponent} from "../../../../shared/form-container/form-container.component";

@Component({
  selector: 'app-variant-update',
  standalone: true,
  imports: [
    BasicInputComponent,
    ImportsModule,
    NgIf,
    BasicButtonComponent,
    FormContainerComponent
  ],
  templateUrl: './variant-update.component.html',
  styleUrl: './variant-update.component.css'
})
export class VariantUpdateComponent implements OnInit,OnChanges {
  @Input() label: string = '';
  @Input() variantId: number = -1;
  @Input() productId: number = -1;
  @Input() variantDetails !: VariantWithOptionsDTO;
  @Output() variantUpdated = new EventEmitter<VariantWithOptionsDTO>();
  @Output() close = new EventEmitter<void>();
  formGroup !: FormGroup;
  isLoading: boolean = false;
  errorMessage: string = '';
  
  ngOnInit(): void {
    // Initial load if data is already available
    if (this.variantDetails) {
      this.fillForm(this.variantDetails);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['variantDetails']?.currentValue) {
      this.fillForm(changes['variantDetails'].currentValue);
    }
  }

  private fillForm(details: VariantWithOptionsDTO) {
    this.formGroup.patchValue({
      title: details.title,
      price: details.price
    });
  }
  constructor(private formBuilder: FormBuilder,private variantService: VariantsService) {
    this.formGroup = this.formBuilder.group({
      title: ['' , Validators.required],
      price: ['' , Validators.required],
    });
  }
  getFieldError(fieldName: string): string {
    const field = this.formGroup.get(fieldName);
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
  onSubmit() {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }
    
    this.isLoading = true;
    this.errorMessage = '';
    const updatedVariant : UpdateVariantRequest = this.formGroup.value;
    
    this.variantService.updateVariant(this.productId, this.variantId, updatedVariant).subscribe({
        next: (response) => {
          const answer:VariantWithOptionsDTO = response.data;
          this.isLoading = false;
          this.variantUpdated.emit(answer);
          this.close.emit();
        },
        error: (error) => {
          console.error('Error updating variant:', error);
          this.errorMessage = error.error?.message || 'Failed to update variant. Please try again.';
          this.isLoading = false;
        }
    });
  }
}
