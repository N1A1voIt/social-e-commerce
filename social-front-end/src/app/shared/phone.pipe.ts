import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'phone',
  standalone:true
})
export class PhonePipe implements PipeTransform {
  transform(value: string | number, countryCode: string = '+1'): string {
    if (!value) return '';

    const cleaned = value.toString().replace(/\D/g, '');

    const match = cleaned.match(/^(\d{3})(\d{3})(\d{4})$/);
    if (match) {
      return `${countryCode} (${match[1]}) ${match[2]}-${match[3]}`;
    }

    return value.toString(); // fallback
  }
}
