import {Component, ElementRef, forwardRef, HostListener, Input, OnInit} from '@angular/core';
import {NG_VALUE_ACCESSOR} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
export interface SelectOption {
  value: any;        // The actual value that gets stored in the form (e.g., category ID)
  label: string;     // The text displayed to the user (e.g., category name)
  description?: string; // Optional additional info shown below the label
}
@Component({
  selector: 'app-basic-select',
  standalone: true,
  imports: [
    NgIf,
    NgForOf
  ],
  templateUrl: './basic-select.component.html',
  styleUrl: './basic-select.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => BasicSelectComponent),
      multi: true,
    },
  ]
})
export class BasicSelectComponent implements OnInit {
  @Input() label!: string;
  @Input() id!: string;
  @Input() placeholder: string = 'Type to search...';
  @Input() errorMessage: string = '';
  @Input() options: SelectOption[] = [];
  @Input() allowCustomValue: boolean = false; // Allow typing custom values
  @Input({transform: (value: false | undefined | boolean): boolean => !!value}) error: boolean = false;

  private _value: any = '';
  displayValue: string = '';
  searchTerm: string = '';
  isOpen: boolean = false;
  filteredOptions: SelectOption[] = [];
  highlightedIndex: number = -1;
  private blurTimeout: any;

  constructor(private elementRef: ElementRef) {}

  ngOnInit() {
    this.filteredOptions = [...this.options];
  }

  get value(): any {
    return this._value;
  }

  set value(val: any) {
    this._value = val;
    this.updateDisplayValue();
    this.onChange(val);
  }

  onChange: (value: any) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: any): void {
    this._value = value;
    this.updateDisplayValue();
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  private updateDisplayValue(): void {
    if (this._value) {
      const selectedOption = this.options.find(opt => opt.value == this._value);
      this.displayValue = selectedOption ? selectedOption.label : String(this._value);
    } else {
      this.displayValue = '';
    }
  }

  onInputChange(event: Event): void {
    const inputValue = (event.target as HTMLInputElement).value;
    this.searchTerm = inputValue;
    this.displayValue = inputValue;
    this.filterOptions();
    this.isOpen = true;
    this.highlightedIndex = -1;

    // If custom values are allowed, update the form value immediately
    if (this.allowCustomValue) {
      this.value = inputValue;
    }
  }

  onFocus(): void {
    if (this.blurTimeout) {
      clearTimeout(this.blurTimeout);
    }
    this.isOpen = true;
    this.filterOptions();
  }

  onBlur(): void {
    // Delay closing to allow for option selection
    this.blurTimeout = setTimeout(() => {
      this.isOpen = false;
      this.onTouched();

      // If not allowing custom values and no valid option selected, clear the field
      if (!this.allowCustomValue && this.searchTerm) {
        const matchingOption = this.options.find(opt =>
          opt.label.toLowerCase() === this.searchTerm.toLowerCase()
        );
        if (!matchingOption) {
          this.displayValue = '';
          this.value = '';
        }
      }
    }, 150);
  }

  onKeyDown(event: KeyboardEvent): void {
    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.highlightNext();
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.highlightPrevious();
        break;
      case 'Enter':
        event.preventDefault();
        if (this.highlightedIndex >= 0 && this.filteredOptions[this.highlightedIndex]) {
          this.selectOption(this.filteredOptions[this.highlightedIndex]);
        }
        break;
      case 'Escape':
        this.isOpen = false;
        break;
    }
  }

  private filterOptions(): void {
    if (!this.searchTerm) {
      this.filteredOptions = [...this.options];
    } else {
      this.filteredOptions = this.options.filter(option =>
        option.label.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (option.description && option.description.toLowerCase().includes(this.searchTerm.toLowerCase()))
      );
    }
  }

  private highlightNext(): void {
    if (this.highlightedIndex < this.filteredOptions.length - 1) {
      this.highlightedIndex++;
    }
  }

  private highlightPrevious(): void {
    if (this.highlightedIndex > 0) {
      this.highlightedIndex--;
    }
  }

  selectOption(option: SelectOption): void {
    this.value = option.value;
    this.displayValue = option.label;
    this.searchTerm = option.label;
    this.isOpen = false;
    this.highlightedIndex = -1;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen = false;
    }
  }

  setDisabledState?(isDisabled: boolean): void {
    // Handle disabled state if needed
  }
}
