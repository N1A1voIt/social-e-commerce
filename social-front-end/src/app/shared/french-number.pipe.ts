import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'frenchNumber',
  standalone: true
})
export class FrenchNumberPipe implements PipeTransform {
  transform(value: number | string | null | undefined, decimals: number = 2): string {
    if (value === null || value === undefined || value === '') {
      return '0';
    }

    const numValue = typeof value === 'string' ? parseFloat(value) : value;

    if (isNaN(numValue)) {
      return '0';
    }

    // Format the number with specified decimal places
    const formatted = numValue.toFixed(decimals);

    // Split into integer and decimal parts
    const parts = formatted.split('.');
    const integerPart = parts[0];
    const decimalPart = parts[1];

    // Add space as thousands separator
    const integerWithSpaces = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');

    // Return with comma as decimal separator (French format)
    return decimalPart ? `${integerWithSpaces},${decimalPart}` : integerWithSpaces;
  }
}

