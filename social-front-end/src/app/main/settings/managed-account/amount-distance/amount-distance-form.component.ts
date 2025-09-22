import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AmountDistanceService } from './amount-distance.service';
import { AmountDistance } from './amount-distance.model';
import { NgIcon } from '@ng-icons/core';
import { BasicInputComponent } from "../../../../shared/basic-input/basic-input.component";
import { BeautifulButtonComponent } from "../../../../shared/beautiful-button/beautiful-button.component";

@Component({
  selector: 'app-amount-distance-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NgIcon,
    BasicInputComponent,
    BeautifulButtonComponent
  ],
  templateUrl: './amount-distance-form.component.html',
  styleUrls: ['./amount-distance-form.component.css']
})
export class AmountDistanceFormComponent implements OnInit {
  @Input() managedPageId!: number;
  @Input() onClose!: () => void;

  amountDistanceForm!: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  existingConfig: AmountDistance | null = null;
  isLoading = true;

  constructor(
    private fb: FormBuilder,
    private amountDistanceService: AmountDistanceService
  ) {}

  ngOnInit(): void {
    this.loadExistingConfig();
  }

  private loadExistingConfig(): void {
    this.isLoading = true;
    this.amountDistanceService.getAmountDistancesByManagedPageId(this.managedPageId).subscribe({
      next: (configs) => {
        if (configs && configs.length > 0) {
          this.existingConfig = configs[0]; // Assuming there's only one config per managed page
        }
        this.initForm();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading existing configuration:', error);
        this.initForm();
        this.isLoading = false;
      }
    });
  }

  private initForm(): void {
    this.amountDistanceForm = this.fb.group({
      id: [this.existingConfig ? this.existingConfig.id : null],
      pricePerDistance: [this.existingConfig ? this.existingConfig.pricePerDistance : 0, [Validators.required, Validators.min(0)]]
    });
  }

  onSubmit(): void {
    if (this.amountDistanceForm.invalid) {
      this.amountDistanceForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const amountDistance: AmountDistance = {
      ...this.amountDistanceForm.value,
      managedPageId: this.managedPageId
    };

    if (amountDistance.id) {
      // Update existing configuration
      this.amountDistanceService.updateAmountDistance(amountDistance.id, amountDistance).subscribe({
        next: (response) => {
          this.isSubmitting = false;
          this.successMessage = 'Amount distance configuration updated successfully!';
          setTimeout(() => {
            if (this.onClose) {
              this.onClose();
            }
          }, 2000);
        },
        error: (error) => {
          this.isSubmitting = false;
          this.errorMessage = error.message || 'Failed to update amount distance configuration. Please try again.';
        }
      });
    } else {
      // Create new configuration
      this.amountDistanceService.createAmountDistance(amountDistance).subscribe({
        next: (response) => {
          this.isSubmitting = false;
          this.successMessage = 'Amount distance configuration created successfully!';
          setTimeout(() => {
            if (this.onClose) {
              this.onClose();
            }
          }, 2000);
        },
        error: (error) => {
          this.isSubmitting = false;
          this.errorMessage = error.message || 'Failed to create amount distance configuration. Please try again.';
        }
      });
    }
  }
}
