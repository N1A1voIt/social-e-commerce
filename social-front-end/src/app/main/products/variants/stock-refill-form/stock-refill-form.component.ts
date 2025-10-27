import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {FormContainerComponent} from '../../../../shared/form-container/form-container.component';
import {BasicInputComponent} from '../../../../shared/basic-input/basic-input.component';
import {BasicButtonComponent} from '../../../../shared/basic-button/basic-button.component';
import {VariantWithOptionsDTO} from '../../products.types';
import {StocksService} from '../../stocks.service';
import {ApiResponse} from '../../../inbox/inbox.service';

@Component({
  selector: 'app-stock-refill-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormContainerComponent,
    BasicInputComponent,
    BasicButtonComponent
  ],
  templateUrl: './stock-refill-form.component.html'
})
export class StockRefillFormComponent implements OnInit, OnChanges {
  @Input() productId: number = -1;
  @Input() variant!: VariantWithOptionsDTO;
  @Input() label: string = 'Refill Stock';
  @Output() close = new EventEmitter<void>();
  @Output() stockSaved = new EventEmitter<void>();

  formGroup!: FormGroup;
  saving = false;

  constructor(private fb: FormBuilder, private stocksService: StocksService) {}

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      description: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    // no-op for now; could prefill description based on variant
  }

  getFieldError(fieldName: string): string {
    const field = this.formGroup.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) return `${fieldName} is required`;
      if (field.errors['min']) return `${fieldName} must be at least ${field.errors['min'].min}`;
    }
    return '';
  }

  onSubmit(): void {
    if (this.formGroup.invalid || !this.variant || this.productId < 0) {
      this.formGroup.markAllAsTouched();
      return;
    }

    const payload = {
      description: this.formGroup.value.description,
      idOrderM: null,
      createdAt: new Date().toISOString(),
      items: [
        {
          idProduct: this.productId,
          idVariant: this.variant.idVariant,
          price: this.variant.price,
          input: this.formGroup.value.quantity,
          output: 0,
          actionAt: new Date().toISOString(),
        }
      ]
    };

    this.saving = true;
    this.stocksService.saveStocks(payload).subscribe({
      next: (_res: ApiResponse) => {
        this.saving = false;
        this.stockSaved.emit();
        this.close.emit();
      },
      error: (err) => {
        console.error('Error saving stock move', err);
        this.saving = false;
      }
    });
  }
}
