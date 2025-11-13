import { Component, Input, Output, EventEmitter } from '@angular/core';
import {CheckboxComponent} from "../../../../shared/checkbox/checkbox.component";
import {NgIf} from "@angular/common";

/**
 * A single contact row. Receives a `phone` object via @Input and
 * emits `edit` when the user clicks the Edit button.
 */
@Component({
  selector: 'app-contect-row',
  standalone: true,
  imports: [
    CheckboxComponent,
    NgIf
  ],
  templateUrl: './contect-row.component.html',
  styleUrls: ['./contect-row.component.css']
})
export class ContectRowComponent {
  @Input() phone: any | null = null;
  @Output() edit = new EventEmitter<any>();

  // Emit directly from the template to avoid unused-method warnings
}
