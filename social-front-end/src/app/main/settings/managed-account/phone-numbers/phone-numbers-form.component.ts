import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PhoneNumbersService } from './phone-numbers.service';
import { PhoneNumber, PhoneNumberPayload } from './phone-numbers.model';
import { NgIcon } from '@ng-icons/core';
import { BasicInputComponent } from "../../../../shared/basic-input/basic-input.component";
import { BeautifulButtonComponent } from "../../../../shared/beautiful-button/beautiful-button.component";
import {BasicSelectComponent, SelectOption} from "../../../../shared/basic-select/basic-select.component";
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { javaHost } from '../../../../../environments/environment';

interface PaymentMethod {
  id: number;
  paymentName: string;
}

@Component({
  selector: 'app-phone-numbers-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NgIcon,
    BasicInputComponent,
    BeautifulButtonComponent,
    BasicSelectComponent
  ],
  templateUrl: './phone-numbers-form.component.html',
  styleUrls: ['./phone-numbers-form.component.css']
})
export class PhoneNumbersFormComponent implements OnInit {
  @Input() managedPageId!: number;
  @Input() onClose!: () => void;

  phoneNumberForm!: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  phoneNumbers: PhoneNumber[] = []; // already assigned phone numbers for this managed page
  isLoading = false;
  selectedPhoneNumber: PhoneNumber | null = null;
  isEditMode = false;
  paymentMethods: PaymentMethod[] = [];
  isLoadingPaymentMethods = false;
  options:SelectOption[] = [];

  // options for the phone-number select: label = phoneNumber (string), value = id (seller phone id)
  optionsPhoneNumbers: SelectOption[] = [];
  phoneNumbersGen: any[] = [];

  constructor(
    private fb: FormBuilder,
    private phoneNumbersService: PhoneNumbersService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadPhoneNumbers();
    this.loadPhoneNumbersGen(); // load generator phone numbers for the select
    this.loadPaymentMethods();
  }

  private initForm(): void {
    // phoneNumber control will store the seller-phone id (number/string) selected from dropdown
    this.phoneNumberForm = this.fb.group({
      phoneNumber: ['', [Validators.required]],
    });
  }

  loadPhoneNumbers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.phoneNumbersService.getPhoneNumbersByManagedPageId(this.managedPageId).subscribe({
      next: (numbers) => {
        this.phoneNumbers = numbers || [];
        this.isLoading = false;
        // recompute available options after assigned numbers load
        this.computeAvailablePhoneOptions();
      },
      error: (error) => {
        this.errorMessage = 'Failed to load phone numbers';
        this.isLoading = false;
        console.error('Error loading phone numbers:', error);
      }
    });
  }

  loadPaymentMethods(): void {
    this.isLoadingPaymentMethods = true;
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', token?.replace('Bearer ', '') || '');

    this.http.get<any>(`${javaHost}/api/payment-methods`, { headers }).subscribe({
      next: (response) => {
        // Handle different response formats
        if (response && Array.isArray(response.data)) {
          this.paymentMethods = response.data;
        } else if (Array.isArray(response)) {
          this.paymentMethods = response;
        } else {
          this.paymentMethods = [];
        }
        console.log(this.paymentMethods)
        this.options = this.paymentMethods.map((number) => ({
          label: number.paymentName,
          value: number.id
        }));
        this.isLoadingPaymentMethods = false;
      },
      error: (error) => {
        console.error('Error loading payment methods:', error);
        this.isLoadingPaymentMethods = false;
      }
    });
  }

  onNewPhoneNumber(): void {
    this.isEditMode = true;
    this.selectedPhoneNumber = null;
    this.phoneNumberForm.reset({
      phoneNumber: '',
      associatedName: '',
      idPm: null
    });
    this.computeAvailablePhoneOptions();
  }

  onEditPhoneNumber(phoneNumber: PhoneNumber): void {
    this.isEditMode = true;
    this.selectedPhoneNumber = phoneNumber;

    // Try to find the generator phone entry that matches the phoneNumber string
    const match = this.phoneNumbersGen.find(pn => (pn.phoneNumber || '').toString() === (phoneNumber.phoneNumber || '').toString());

    // cast to any to allow accessing optional sellerId that isn't on the PhoneNumber type
    const phoneValue = match ? match.id : ((phoneNumber as any).sellerId ?? phoneNumber.id ?? phoneNumber.phoneNumber);

    this.phoneNumberForm.patchValue({
      phoneNumber: phoneValue,
      associatedName: phoneNumber.associatedName,
      idPm: phoneNumber.idPm
    });

    this.computeAvailablePhoneOptions();
  }

  onDeletePhoneNumber(phoneNumber: PhoneNumber): void {
    if (!phoneNumber.id) return;

    if (!confirm(`Are you sure you want to delete the phone number ${phoneNumber.phoneNumber}?`)) {
      return;
    }

    this.phoneNumbersService.deletePhoneNumber(phoneNumber.id).subscribe({
      next: () => {
        this.successMessage = 'Phone number deleted successfully!';
        this.loadPhoneNumbers();
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.errorMessage = 'Failed to delete phone number';
        console.error('Error deleting phone number:', error);
      }
    });
  }

  onCancelEdit(): void {
    this.isEditMode = false;
    this.selectedPhoneNumber = null;
    this.phoneNumberForm.reset();
  }

  // generator phone numbers (from /api/sellers/phone-numbers)
  loadPhoneNumbersGen() {
    this.isLoading = true;
    const token = localStorage.getItem('token') || '';
    const headers = new HttpHeaders({ 'Authorization': token });
    this.http.get(`${javaHost}/api/sellers/phone-numbers`, { headers }).subscribe({
      next: (res: any) => {
        if (res && res.status === 200 && Array.isArray(res.data)) {
          this.phoneNumbersGen = res.data;
        } else if (Array.isArray(res)) {
          // fallback if backend returns raw array
          this.phoneNumbersGen = res;
        }
        // recompute select options after generator list loads
        this.computeAvailablePhoneOptions();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load phone numbers', err);
        this.isLoading = false;
      }
    });
  }

  private computeAvailablePhoneOptions(): void {
    const gen = this.phoneNumbersGen || [];
    const assigned = this.phoneNumbers || [];

    // build set of assigned phone number strings to exclude
    const assignedSet = new Set(assigned.map(a => (a.phoneNumber || '').toString()));

    const available = gen.filter((pn: any) => {
      const pnValue = (pn.phoneNumber || '').toString();
      return !assignedSet.has(pnValue) || this.selectedPhoneNumber?.phoneNumber === pnValue;
    });

    // options: value = id (seller phone id), label = phoneNumber string
    this.optionsPhoneNumbers = available.map((pn: any) => ({
      label: pn.phoneNumber,
      value: pn.id
    }));
  }

  onSubmit(): void {
    if (this.phoneNumberForm.invalid) {
      this.phoneNumberForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    // If the form stores the seller phone id as the phoneNumber control, map it back
    const selectedPhoneId = this.phoneNumberForm.value.phoneNumber;
    let phoneString = this.phoneNumberForm.value.phoneNumber;
    if (selectedPhoneId !== undefined && selectedPhoneId !== null) {
      const found = this.phoneNumbersGen.find(p => p.id === selectedPhoneId || p.id?.toString() === selectedPhoneId?.toString());
      if (found) phoneString = found.phoneNumber;
    }

    const payload: PhoneNumberPayload = {
      idSpn: selectedPhoneId,
      idPm: -1,
      idMp: this.managedPageId
    };

    const operation = this.selectedPhoneNumber?.id
      ? this.phoneNumbersService.updatePhoneNumber(this.selectedPhoneNumber.id, payload)
      : this.phoneNumbersService.createPhoneNumber(payload);

    operation.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = this.selectedPhoneNumber?.id
          ? 'Phone number updated successfully!'
          : 'Phone number created successfully!';
        this.phoneNumberForm.reset();
        this.isEditMode = false;
        this.selectedPhoneNumber = null;
        this.loadPhoneNumbers();
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.message || 'Failed to save phone number. Please try again.';
      }
    });
  }

  getPaymentMethodName(idPm: number): string {
    const method = this.paymentMethods.find(pm => pm.id === idPm);
    return method ? method.paymentName : 'Unknown';
  }
}
