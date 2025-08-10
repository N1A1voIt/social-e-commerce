import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {VariantsService} from "./variants.service";
import {VariantWithOptionsDTO, CreateVariantWithOptionsRequest, GenerateVariantsRequest, UpdateVariantRequest} from "../products.types";
import {NgIf} from "@angular/common";
import {VariantListComponent} from "./variant-list/variant-list.component";
import {ApiResponse} from "../../inbox/inbox.service";

@Component({
  selector: 'app-variants',
  standalone: true,
  imports: [
    NgIf,
    VariantListComponent
  ],
  templateUrl: './variants.component.html',
  styleUrl: './variants.component.css'
})
export class VariantsComponent implements OnInit {
  variants: VariantWithOptionsDTO[] = [];
  loading: boolean = false;
  productId: number = 0;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private variantService: VariantsService,
    private cdr:ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.productId = Number(this.route.snapshot.paramMap.get('idProduct'));
    if (this.productId) {
      this.fetchVariants();
    } else {
      this.errorMessage = 'Invalid product ID';
    }
  }

  fetchVariants(): void {
    this.loading = true;
    this.errorMessage = '';

    this.variantService.fetchVariantsWithOptions(this.productId).subscribe({
      next: (variants:ApiResponse) => {
        this.variants = variants.data;
        console.log('Fetched variants:', variants);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching variants:', error);
        this.errorMessage = error.error?.message || 'Failed to load variants';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onCreateVariant(): void {
    // TODO: Implement create variant modal/form
    console.log('Create variant clicked');
    this.showMessage('Create variant feature coming soon!', 'info');
  }

  onGenerateAllVariants(): void {
    if (confirm('This will generate all possible variant combinations. Continue?')) {
      this.loading = true;

      const request: GenerateVariantsRequest = {
        basePrice: 0, // TODO: Get from form or product
        titlePrefix: '',
        overwriteExisting: false
      };

      this.variantService.generateAllVariants(this.productId, request).subscribe({
        next: (variants: VariantWithOptionsDTO[]) => {
          this.variants = variants;
          this.loading = false;
          this.showMessage(`Successfully generated ${variants.length} variants!`, 'success');
        },
        error: (error) => {
          console.error('Error generating variants:', error);
          this.errorMessage = error.error?.message || 'Failed to generate variants';
          this.loading = false;
        }
      });
    }
  }

  onEditVariant(variant: VariantWithOptionsDTO): void {
    // TODO: Implement edit variant modal/form
    console.log('Edit variant:', variant);
    this.showMessage('Edit variant feature coming soon!', 'info');
  }

  onDeleteVariant(variant: VariantWithOptionsDTO): void {
    this.loading = true;

    this.variantService.deleteVariant(this.productId, variant.idVariant).subscribe({
      next: (response) => {
        this.variants = this.variants.filter(v => v.idVariant !== variant.idVariant);
        this.loading = false;
        this.showMessage(`Variant "${variant.title}" deleted successfully!`, 'success');
      },
      error: (error) => {
        console.error('Error deleting variant:', error);
        this.errorMessage = error.error?.message || 'Failed to delete variant';
        this.loading = false;
      }
    });
  }

  private showMessage(message: string, type: 'success' | 'error' | 'info'): void {
    if (type === 'success') {
      this.successMessage = message;
      this.errorMessage = '';
      // Clear success message after 3 seconds
      setTimeout(() => {
        this.successMessage = '';
      }, 3000);
    } else if (type === 'error') {
      this.errorMessage = message;
      this.successMessage = '';
    } else {
      // For info messages, we can use success styling but clear faster
      this.successMessage = message;
      this.errorMessage = '';
      setTimeout(() => {
        this.successMessage = '';
      }, 2000);
    }
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
