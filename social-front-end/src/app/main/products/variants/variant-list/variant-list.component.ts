import {Component, Input, Output, EventEmitter} from '@angular/core';
import {NgForOf, NgIf, CurrencyPipe, DatePipe} from "@angular/common";
import {VariantWithOptionsDTO} from "../../products.types";

@Component({
  selector: 'app-variant-list',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    CurrencyPipe,
    DatePipe
  ],
  templateUrl: './variant-list.component.html',
  styleUrl: './variant-list.component.css'
})
export class VariantListComponent {
  @Input() variantsList: VariantWithOptionsDTO[] = [];
  @Input() loading: boolean = false;
  @Output() editVariant = new EventEmitter<VariantWithOptionsDTO>();
  @Output() deleteVariant = new EventEmitter<VariantWithOptionsDTO>();
  @Output() createVariant = new EventEmitter<void>();
  @Output() generateAllVariants = new EventEmitter<void>();

  onEditVariant(variant: VariantWithOptionsDTO) {
    this.editVariant.emit(variant);
  }

  onDeleteVariant(variant: VariantWithOptionsDTO) {
    if (confirm(`Are you sure you want to delete variant "${variant.title}"?`)) {
      this.deleteVariant.emit(variant);
    }
  }

  onCreateVariant() {
    this.createVariant.emit();
  }

  onGenerateAllVariants() {
    this.generateAllVariants.emit();
  }

  getOptionsDisplay(variant: VariantWithOptionsDTO): string {
    if (!variant.options || variant.options.length === 0) {
      return 'No options';
    }
    return variant.options
      .map(option => `${option.optionLabel}: ${option.optionValue}`)
      .join(', ');
  }

  getStockStatusClass(status?: string): string {
    switch (status?.toLowerCase()) {
      case 'in stock':
        return 'bg-green-100 text-green-800';
      case 'low stock':
        return 'bg-yellow-100 text-yellow-800';
      case 'out of stock':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStockStatusText(status?: string): string {
    switch (status?.toLowerCase()) {
      case 'in_stock':
        return 'In Stock';
      case 'low_stock':
        return 'Low Stock';
      case 'out_of_stock':
        return 'Out of Stock';
      default:
        return 'Unknown';
    }
  }

  trackByVariantId(index: number, variant: VariantWithOptionsDTO): number {
    return variant.idVariant;
  }
}
