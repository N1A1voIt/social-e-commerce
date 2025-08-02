import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgIf} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {animate, query, stagger, state, style, transition, trigger} from "@angular/animations";
import {Product} from "../../../products/products.types";
@Component({
  selector: 'app-prompt-form',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    FormsModule
  ],animations: [
    trigger('containerAnimation', [
      state('collapsed', style({ transform: 'scale(1)' })),
      state('expanded', style({ transform: 'scale(1)' })),
      transition('collapsed => expanded', animate('300ms ease-out')),
      transition('expanded => collapsed', animate('300ms ease-in'))
    ]),

    trigger('buttonBarAnimation', [
      state('visible', style({ opacity: 1, transform: 'translateY(0)' })),
      state('hidden', style({ opacity: 0, transform: 'translateY(-20px)' })),
      transition('visible => hidden', animate('200ms ease-in')),
      transition('hidden => visible', animate('300ms ease-out'))
    ]),

    trigger('formAnimation', [
      state('visible', style({ opacity: 1, transform: 'translateY(0) scale(1)' })),
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(20px) scale(0.95)' }),
        animate('400ms cubic-bezier(0.4, 0, 0.2, 1)',
          style({ opacity: 1, transform: 'translateY(0) scale(1)' }))
      ]),
      transition(':leave', [
        animate('300ms ease-in',
          style({ opacity: 0, transform: 'translateY(-20px) scale(0.95)' }))
      ])
    ]),

    trigger('slideDown', [
      state('visible', style({ opacity: 1, transform: 'translateY(0)' })),
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-10px)' }),
        animate('300ms 100ms ease-out',
          style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ]),

    trigger('staggeredItems', [
      state('visible', style({})),
      transition(':enter', [
        query(':enter', [
          style({ opacity: 0, transform: 'translateY(20px)' }),
          stagger(100, [
            animate('400ms ease-out',
              style({ opacity: 1, transform: 'translateY(0)' }))
          ])
        ], { optional: true })
      ])
    ])
  ],
  templateUrl: './prompt-form.component.html',
  styleUrl: './prompt-form.component.css'
})
export class PromptFormComponent {
  @Input() step: string = 'platforms';
  @Input() products: Product[] = [];
  @Output() stepChange = new EventEmitter<string>();

  showForm: boolean = false;
  promptText: string = '';
  includeContext: boolean = false;

  updateStep(newStep: string) {
    this.step = newStep;
    this.stepChange.emit(newStep);
  }

  toggleForm() {
    this.showForm = !this.showForm;
    if (!this.showForm) {
      this.clearForm();
    }
  }

  clearForm() {
    this.promptText = '';
    this.includeContext = false;
  }

  submitPrompt() {
    if (this.promptText.trim()) {
      console.log('Submitting prompt:', {
        text: this.promptText,
        step: this.step,
        includeContext: this.includeContext
      });
      // Add your submission logic here
      this.clearForm();
      this.showForm = false;
    }
  }

  onEnterKey(event: KeyboardEvent) {
    if (event.ctrlKey || event.metaKey) {
      event.preventDefault();
      this.submitPrompt();
    }
  }
}
