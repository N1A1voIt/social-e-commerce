import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {javaHost} from '../../../../environments/environment';
import {ProductCpl} from '../products.types';
import {NgForOf, NgIf} from '@angular/common';
import {BasicInputComponent} from '../../../shared/basic-input/basic-input.component';
import {BasicButtonComponent} from '../../../shared/basic-button/basic-button.component';

export interface Option {
  idOption?: number;
  optionLabels: string;
  values: string[];
}

@Component({
  selector: 'app-edit-product-options',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    NgForOf,
    BasicInputComponent,
    BasicButtonComponent
  ],
  template: `
    <form [formGroup]="optionsForm" (ngSubmit)="onSubmit()" class="space-y-4 w-full flex flex-col gap-1">
      <div class="mb-4">
        <h3 class="text-lg font-medium text-gray-900 mb-2">Edit Product Options</h3>
        <p class="text-sm text-gray-600">Modify existing options or add new ones for your product.</p>
      </div>

      <!-- Loading indicator for options -->
      <div *ngIf="isLoading" class="flex justify-center items-center py-8">
        <div class="text-center">
          <svg class="animate-spin h-8 w-8 mx-auto mb-2 text-blue-600" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <p class="text-sm text-gray-600">Loading options...</p>
        </div>
      </div>

      <div *ngIf="!isLoading" formArrayName="options" class="space-y-4">
        <div *ngFor="let option of optionsArray.controls; let i = index"
             [formGroupName]="i"
             class="border border-gray-200 rounded-lg p-4">

          <div class="flex justify-between items-center mb-3">
            <h4 class="font-medium text-gray-800">Option {{i + 1}}</h4>
            <button type="button"
                    (click)="removeOption(i)"
                    class="text-red-500 hover:text-red-700 text-sm">
              Remove
            </button>
          </div>

          <app-basic-input
            class="w-full mb-3"
            label="Option Label"
            placeholder="e.g., Size, Color, Material"
            [formControl]="$any(option.get('optionLabels'))">
          </app-basic-input>

          <div formArrayName="values" class="space-y-2">
            <label class="block text-sm font-medium text-gray-700 mb-2">Values</label>
            <div *ngFor="let value of getValuesArray(i).controls; let j = index"
                 class="flex items-center space-x-2">
              <app-basic-input
                class="flex-1"
                placeholder="Enter value"
                [formControl]="$any(value)">
              </app-basic-input>
              <button type="button"
                      (click)="removeValue(i, j)"
                      class="text-red-500 hover:text-red-700 text-sm px-2">
                ×
              </button>
            </div>
            <button type="button"
                    (click)="addValue(i)"
                    class="text-blue-500 hover:text-blue-700 text-sm font-medium">
              + Add Value
            </button>
          </div>
        </div>
      </div>

      <button *ngIf="!isLoading" type="button"
              (click)="addOption()"
              class="w-full border-2 border-dashed border-gray-300 rounded-lg p-4 text-gray-500 hover:border-gray-400 hover:text-gray-600">
        + Add Option
      </button>

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
          [disabled]="optionsForm.invalid || isLoading"
          [loading]="isLoading">
          Update Options
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
export class EditProductOptionsComponent implements OnInit {
  @Input() product!: ProductCpl;
  @Output() formClosed = new EventEmitter<void>();
  @Output() optionsUpdated = new EventEmitter<any>();

  optionsForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient
  ) {}

  ngOnInit() {
    console.log('EditProductOptionsComponent ngOnInit called with product:', this.product);
    this.initializeForm();
    this.loadProductOptions();
  }

  private initializeForm() {
    this.optionsForm = this.fb.group({
      options: this.fb.array([])
    });
  }

  get optionsArray(): FormArray {
    return this.optionsForm.get('options') as FormArray;
  }

  getValuesArray(optionIndex: number): FormArray {
    return this.optionsArray.at(optionIndex).get('values') as FormArray;
  }

  private loadProductOptions() {
    this.isLoading = true;
    this.errorMessage = '';

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    });

    this.http.get<any>(`${javaHost}/api/options/product/${this.product.idPc}`, { headers })
      .subscribe({
        next: (response) => {
          console.log('Loaded options response:', response);
          const options = response.data || [];
          this.populateOptionsForm(options);
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading options:', error);
          this.errorMessage = 'Failed to load product options';
          this.isLoading = false;
        }
      });
  }

  private populateOptionsForm(options: any[]) {
    // Clear existing options
    while (this.optionsArray.length !== 0) {
      this.optionsArray.removeAt(0);
    }

    // Add loaded options
    options.forEach(option => {
      const optionGroup = this.fb.group({
        idOption: [option.idOption],
        optionLabels: [option.label, Validators.required],
        values: this.fb.array(
          option.optionValues?.map((ov: any) => new FormControl(ov.value, Validators.required)) || []
        )
      });
      this.optionsArray.push(optionGroup);
    });

    // If no options exist, add one empty option
    if (this.optionsArray.length === 0) {
      this.addOption();
    }
  }

  addOption() {
    const optionGroup = this.fb.group({
      idOption: [null],
      optionLabels: ['', Validators.required],
      values: this.fb.array([new FormControl('', Validators.required)])
    });
    this.optionsArray.push(optionGroup);
  }

  removeOption(index: number) {
    this.optionsArray.removeAt(index);
  }

  addValue(optionIndex: number) {
    const valuesArray = this.getValuesArray(optionIndex);
    valuesArray.push(new FormControl('', Validators.required));
  }

  removeValue(optionIndex: number, valueIndex: number) {
    const valuesArray = this.getValuesArray(optionIndex);
    if (valuesArray.length > 1) {
      valuesArray.removeAt(valueIndex);
    }
  }

  onSubmit() {
    if (this.optionsForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const formData = this.optionsForm.value;

      // Transform the options data to match the backend API
      const optionsData = formData.options.map((option: any) => ({
        idOption: option.idOption, // Include ID for existing options
        label: option.optionLabels,
        optionValues: option.values
          .filter((value: string) => value && value.trim() !== '')
          .map((value: string) => ({
            value: value.trim()
          }))
      }));

      console.log('Sending options data:', optionsData);

      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      });

      this.http.put(`${javaHost}/api/products/${this.product.idPc}/options`, optionsData, { headers })
        .subscribe({
          next: (response: any) => {
            console.log('Options updated successfully:', response);
            this.successMessage = 'Product options updated successfully!';
            this.isLoading = false;

            // Emit the updated options
            this.optionsUpdated.emit(optionsData);

            // Close form after 1.5 seconds
            setTimeout(() => {
              this.onCancel();
            }, 1500);
          },
          error: (error) => {
            console.error('Error updating options:', error);
            this.errorMessage = error.error?.message || 'Failed to update product options. Please try again.';
            this.isLoading = false;
          }
        });
    }
  }

  onCancel() {
    this.formClosed.emit();
  }
}
