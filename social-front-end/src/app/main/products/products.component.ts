import {Component, OnInit} from '@angular/core';
import {ProductListComponent} from "./product-list/product-list.component";
import {FormContainerComponent} from "../../shared/form-container/form-container.component";
import {BasicButtonComponent} from "../../shared/basic-button/basic-button.component";
import {BasicInputComponent} from "../../shared/basic-input/basic-input.component";
import {AsyncPipe, NgForOf, NgIf} from "@angular/common";
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {javaHost} from "../../../environments/environment";
import { TempProduct, OptionValueDTO, CreationStepsDTO } from "./products.types"
import {SupabaseService} from "../../shared/supabase.service";

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [
    ProductListComponent,
    FormContainerComponent,
    BasicButtonComponent,
    BasicInputComponent,
    NgIf,
    ReactiveFormsModule,
    NgForOf,
    AsyncPipe
  ],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css'
})
export class ProductsComponent implements OnInit {
  currentStep = 1;
  step1Form!: FormGroup;
  step2Form!: FormGroup;
  isLoading = false;
  errorMessage = '';
  sessionId = '';


  selectedFile: File | null = null;
  previewUrl: string | null = null;
  isUploadingFile = false;
  uploadError = '';


  private apiUrl = javaHost + '/api/steps'; // Adjust base URL as needed
  private authToken = ''; // Get from your auth service

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private supabaseService: SupabaseService
  ) {
    this.initializeForms();
  }

  ngOnInit() {
    // Get auth token from your authentication service
    this.authToken = this.getAuthToken();

    // Try to recover existing data on component initialization
    this.recoverData();
  }

  private initializeForms() {
    this.step1Form = this.fb.group({
      idProduct: [],
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      media: [''],
      idSeller: [1], // Set from current user context
      idCategory: [1, [Validators.required, Validators.min(1)]],
      state: [true]
    });

    this.step2Form = this.fb.group({
      options: this.fb.array([])
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
    this.step1Form.patchValue({ media: '' });
  }

  async uploadFileToSupabase(): Promise<string | null> {
    if (!this.selectedFile) {
      return null;
    }

    try {
      this.isUploadingFile = true;
      this.uploadError = '';

      const mediaUrl = await this.supabaseService.uploadFile(this.selectedFile, 'products');

      // Update form with the uploaded file URL
      this.step1Form.patchValue({ media: mediaUrl });

      return mediaUrl;
    } catch (error) {
      console.error('Upload error:', error);
      this.uploadError = 'Failed to upload image. Please try again.';
      return null;
    } finally {
      this.isUploadingFile = false;
    }
  }

  get optionsArray() {
    return this.step2Form.get('options') as FormArray;
  }

  getValuesArray(optionIndex: number) {
    return this.optionsArray.at(optionIndex).get('values') as FormArray;
  }

  addOption() {
    const optionGroup = this.fb.group({
      optionLabels: ['', Validators.required],
      values: this.fb.array([this.fb.control('', Validators.required)])
    });
    this.optionsArray.push(optionGroup);
  }

  removeOption(index: number) {
    this.optionsArray.removeAt(index);
  }

  addValue(optionIndex: number) {
    const valuesArray = this.getValuesArray(optionIndex);
    valuesArray.push(this.fb.control('', Validators.required));
  }

  removeValue(optionIndex: number, valueIndex: number) {
    const valuesArray = this.getValuesArray(optionIndex);
    if (valuesArray.length > 1) {
      valuesArray.removeAt(valueIndex);
    }
  }

  private getHeaders() {
    return new HttpHeaders({
      'Authorization': this.authToken,
      'Content-Type': 'application/json'
    });
  }

  async saveStep1() {
    if (this.step1Form.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      try {
        // Upload file first if there's a selected file
        if (this.selectedFile) {
          const uploadedUrl = await this.uploadFileToSupabase();
          if (!uploadedUrl) {
            this.isLoading = false;
            return; // Stop if upload failed
          }
          this.step1Form.patchValue({ media: uploadedUrl });
        }

        const step1Data: CreationStepsDTO = {
          sessionId: this.sessionId,
          step1: this.step1Form.value,
          step2: []
        };

        this.http.post<CreationStepsDTO>(`${this.apiUrl}/step1`, step1Data, { headers: this.getHeaders() })
          .subscribe({
            next: (response) => {
              this.sessionId = response.sessionId;
              this.currentStep = 2;
              this.isLoading = false;

              // Initialize with at least one option
              if (this.optionsArray.length === 0) {
                this.addOption();
              }
            },
            error: (error) => {
              this.errorMessage = 'Failed to save step 1. Please try again.';
              this.isLoading = false;
              console.error('Step 1 save error:', error);
            }
          });
      } catch (error) {
        this.errorMessage = 'An error occurred while processing your request.';
        this.isLoading = false;
        console.error('Save step 1 error:', error);
      }
    } else {
      this.markFormGroupTouched(this.step1Form);
    }
  }

  saveStep2() {
    if (this.step2Form.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const step2Data: CreationStepsDTO = {
        sessionId: this.sessionId,
        step1: this.step1Form.value,
        step2: this.step2Form.value.options
      };

      this.http.post<CreationStepsDTO>(`${this.apiUrl}/step2`, step2Data, { headers: this.getHeaders() })
        .subscribe({
          next: (response) => {
            this.currentStep = 3;
            this.isLoading = false;
          },
          error: (error) => {
            this.errorMessage = 'Failed to save step 2. Please try again.';
            this.isLoading = false;
            console.error('Step 2 save error:', error);
          }
        });
    } else {
      this.markFormGroupTouched(this.step2Form);
    }
  }

  recoverData() {
    this.isLoading = true;
    this.errorMessage = '';

    this.http.get<CreationStepsDTO>(`${this.apiUrl}/recovery`, { headers: this.getHeaders() })
      .subscribe({
        next: (response) => {
          if (response.step1) {
            this.step1Form.patchValue(response.step1);
            this.sessionId = response.sessionId;

            if (response.step2 && response.step2.length > 0) {
              this.populateStep2Form(response.step2);
              this.currentStep = 2;
            }
          }
          this.isLoading = false;
        },
        error: (error) => {
          // Don't show error for recovery as it might be expected (no data to recover)
          this.isLoading = false;
          console.log('No data to recover or recovery failed:', error);
        }
      });
  }

  private populateStep2Form(options: OptionValueDTO[]) {
    const optionsArray = this.step2Form.get('options') as FormArray;
    optionsArray.clear();

    options.forEach(option => {
      const valuesArray = this.fb.array(
        option.values.map(value => this.fb.control(value, Validators.required))
      );

      const optionGroup = this.fb.group({
        optionLabels: [option.optionLabels, Validators.required],
        values: valuesArray
      });

      optionsArray.push(optionGroup);
    });
  }

  previousStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  resetForm() {
    this.currentStep = 1;
    this.sessionId = '';
    this.errorMessage = '';
    this.initializeForms();
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormArray) {
        control.controls.forEach(arrayControl => {
          if (arrayControl instanceof FormGroup) {
            this.markFormGroupTouched(arrayControl);
          } else {
            arrayControl.markAsTouched();
          }
        });
      }
    });
  }

  private getAuthToken(): string {
    // Implement your token retrieval logic here
    // This could be from localStorage, a service, etc.
    return localStorage.getItem('token') || '';
  }
}
