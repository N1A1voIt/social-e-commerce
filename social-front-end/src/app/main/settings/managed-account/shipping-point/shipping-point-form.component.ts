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
  shippingPoints: ShippingPoint[] = [];
  isLoading = false;
  selectedShippingPoint: ShippingPoint | null = null;
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private shippingPointService: ShippingPointService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadShippingPoints();
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

  loadShippingPoints(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.shippingPointService.getShippingPointsByManagedPageId(this.managedPageId).subscribe({
      next: (points) => {
        this.shippingPoints = points;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load shipping points';
        this.isLoading = false;
        console.error('Error loading shipping points:', error);
      }
    });
  }

  onNewShippingPoint(): void {
    this.isEditMode = true;
    this.selectedShippingPoint = null;
    this.shippingPointForm.reset({
      placeName: '',
      latitude: null,
      longitude: null,
      distance: 0,
      origin: ''
    });
  }

  onEditShippingPoint(point: ShippingPoint): void {
    this.isEditMode = true;
    this.selectedShippingPoint = point;
    this.shippingPointForm.patchValue({
      placeName: point.placeName,
      latitude: point.latitude,
      longitude: point.longitude,
      distance: point.distance,
      origin: point.origin
    });
  }

  onCancelEdit(): void {
    this.isEditMode = false;
    this.selectedShippingPoint = null;
    this.shippingPointForm.reset();
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

    // If editing existing point, include the ID
    if (this.selectedShippingPoint?.id) {
      shippingPoint.id = this.selectedShippingPoint.id;
    }

    this.shippingPointService.createShippingPoint(shippingPoint).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = this.selectedShippingPoint?.id
          ? 'Shipping point updated successfully!'
          : 'Shipping point created successfully!';
        this.shippingPointForm.reset();
        this.isEditMode = false;
        this.selectedShippingPoint = null;
        // Reload the shipping points list
        this.loadShippingPoints();
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.message || 'Failed to save shipping point. Please try again.';
      }
    });
  }
}
