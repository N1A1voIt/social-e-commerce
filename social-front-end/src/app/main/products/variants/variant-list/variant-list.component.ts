import {Component, Input, Output, EventEmitter, HostListener} from '@angular/core';
import {NgForOf, NgIf, DecimalPipe, DatePipe, NgClass} from "@angular/common";
import {VariantWithOptionsDTO} from "../../products.types";
import {VariantFormComponent} from "../variant-form/variant-form.component";
import {VariantUpdateComponent} from "../variant-update/variant-update.component";
import {StockRefillFormComponent} from "../stock-refill-form/stock-refill-form.component";
import {FrenchNumberPipe} from "../../../../shared/french-number.pipe";

@Component({
  selector: 'app-variant-list',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    DecimalPipe,
    DatePipe,
    VariantFormComponent,
    VariantUpdateComponent,
    StockRefillFormComponent,
    FrenchNumberPipe,
    NgClass
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
  @Output() navigateToVariants = new EventEmitter<number>();
  @Input() productId: number = -1;
  // New: notify parent after a successful refill
  @Output() stockRefilled = new EventEmitter<void>();
  // New: notify parent after a successful variant update
  @Output() variantUpdated = new EventEmitter<void>();

  isFormVisible: boolean = false;
  isUpdateFormVisible:boolean = false;
  variantId: number = -1;
  actualVariant !: VariantWithOptionsDTO;
  // New: refill modal state
  isRefillVisible: boolean = false;
  selectedVariant!: VariantWithOptionsDTO;
  // Menu state for dropdown
  openMenuId: number | null = null;

  toggleMenu(variantId: number, event: Event) {
    event.stopPropagation();
    this.openMenuId = this.openMenuId === variantId ? null : variantId;
  }

  isMenuOpen(variantId: number): boolean {
    return this.openMenuId === variantId;
  }

  onEditVariant(variant: VariantWithOptionsDTO, event?: Event) {
    event?.stopPropagation();
    console.log('Edit variant clicked:', variant);
    this.variantId = variant.idVariant;
    this.actualVariant = variant;
    this.isUpdateFormVisible = true;
    this.openMenuId = null;
    console.log('isUpdateFormVisible set to:', this.isUpdateFormVisible);
    console.log('actualVariant:', this.actualVariant);
    this.editVariant.emit(variant);
  }

  onDeleteVariant(variant: VariantWithOptionsDTO, event?: Event) {
    event?.stopPropagation();
    this.openMenuId = null;
    if (confirm(`Are you sure you want to delete variant "${variant.title}"?`)) {
      this.deleteVariant.emit(variant);
    }
  }

  onCreateVariant() {
    this.isFormVisible = true;
    this.createVariant.emit();
  }

  onGenerateAllVariants() {
    this.generateAllVariants.emit();
  }

  // New: open refill modal
  onRefillVariant(variant: VariantWithOptionsDTO, event?: Event) {
    event?.stopPropagation();
    this.selectedVariant = variant;
    this.isRefillVisible = true;
    this.openMenuId = null;
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

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    // Close menu when clicking outside
    this.openMenuId = null;
  }
}
