import {Component, Input} from '@angular/core';
import {FormArray, FormGroup} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-form-validation-summary',
  standalone: true,
  imports: [
    NgIf,
    NgForOf
  ],
  templateUrl: './form-validation-summary.component.html',
  styleUrl: './form-validation-summary.component.css'
})
export class FormValidationSummaryComponent {
  @Input() form!: FormGroup;

  get mediaDetailsArray(): FormArray {
    return this.form.get('mediaDetails') as FormArray;
  }
}
