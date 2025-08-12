import {Component, forwardRef, Input} from '@angular/core';
import {NG_VALUE_ACCESSOR} from "@angular/forms";

@Component({
  selector: 'app-basic-input',
  standalone: true,
  imports: [],
  templateUrl: './basic-input.component.html',
  styleUrl: './basic-input.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => BasicInputComponent),
      multi: true,
    },
  ],
})
export class BasicInputComponent {
  @Input() label!: string;
  @Input() type!: string;
  @Input() id!: string;
  @Input() placeholder!: string;
  @Input() errorMessage: string = '';
  @Input({transform: (value: false | undefined | boolean): boolean => false}) error: boolean = false;

  private _value: any = '';

  get value(): any {
    return this._value;
  }

  set value(val: any) {
    this._value = val;
    this.onChange(val); // Notify the form of the value change
  }

  onChange: (value: any) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: any): void {
    this._value = value; // Update internal value
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  onInputChange(event: Event) {
    const inputValue = (event.target as HTMLInputElement).value;
    this.value = inputValue; // Use the setter to update the value
    this.onChange(inputValue); // Notify Angular form about the value change
    this.onTouched(); // Notify that the input has been touched
  }
}
