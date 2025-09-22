import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ShippingPointService } from './shipping-point.service';
import { ShippingPoint } from './shipping-point.model';
import { NgIcon } from '@ng-icons/core';
import {BasicInputComponent} from "../../../../shared/basic-input/basic-input.component";
import {BeautifulButtonComponent} from "../../../../shared/beautiful-button/beautiful-button.component";

@Component({
  selector: 'app-shipping-point-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NgIcon,
    BasicInputComponent,
    BeautifulButtonComponent
  ],
  templateUrl: './shipping-point-form.component.html',
  styleUrls: ['./shipping-point-form.component.css']
})
export class ShippingPointFormComponent implements OnInit {
  @Input() managedPageId!: number;
  @Input() onClose!: () => void;

  shippingPointForm!: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private shippingPointService: ShippingPointService
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.shippingPointForm = this.fb.group({
      placeName: ['', [Validators.required]],
      latitude: [null],
      longitude: [null],
      distance: [0, [Validators.required, Validators.min(0)]],
      origin: ['']
    });
  }

  onSubmit(): void {
    if (this.shippingPointForm.invalid) {
      this.shippingPointForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const shippingPoint: ShippingPoint = {
      ...this.shippingPointForm.value,
      managedPageId: this.managedPageId
    };

    this.shippingPointService.createShippingPoint(shippingPoint).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = 'Shipping point created successfully!';
        this.shippingPointForm.reset();
        setTimeout(() => {
          if (this.onClose) {
            this.onClose();
          }
        }, 2000);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.message || 'Failed to create shipping point. Please try again.';
      }
    });
  }
}
