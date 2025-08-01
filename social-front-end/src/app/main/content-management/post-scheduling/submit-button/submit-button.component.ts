import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-submit-button',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './submit-button.component.html',
  styleUrl: './submit-button.component.css'
})
export class SubmitButtonComponent {
  @Input() disabled = false;
  @Input() isSubmitting = false;
  @Input() buttonText = 'Create Post';
  @Input() loadingText = 'Creating Post...';
  @Output() submit = new EventEmitter<void>();

  onSubmit(): void {
    this.submit.emit();
  }
}
