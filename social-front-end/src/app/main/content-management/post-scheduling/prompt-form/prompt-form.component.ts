import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgIf} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {animate, query, stagger, state, style, transition, trigger} from "@angular/animations";
import {Product} from "../../../products/products.types";
import {HttpClient} from "@angular/common/http";
import {pythonHost} from "../../../../../environments/environment";
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
  @Input() loading:boolean = false;
  @Input() parsedJson:any;
  @Output() loadingChange = new EventEmitter<boolean>();
  @Output() formValueChange = new EventEmitter<any>();
  showForm: boolean = false;
  promptText: string = '';
  includeContext: boolean = false;

  constructor(private http: HttpClient) {}

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
    const headers = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    this.loading = true;
    this.loadingChange.emit(this.loading);
    this.http.post<string>(pythonHost + '/generate-post', { query: this.promptText },{headers:headers}).subscribe({
      next: (response) => {
        try {
          console.log(response)
          const jsonString = response.replace(/^```json\s*|\s*```$/g, '');
          const parsedResponse = JSON.parse(jsonString);
          this.loading = false;
          this.parsedJson = parsedResponse;
          this.loadingChange.emit(this.loading);
          this.formValueChange.emit(parsedResponse);
        } catch (error) {
          console.error('Error parsing response:error'+error);
          alert('AI is not perfect, try again!');
          this.loading = false;
          this.loadingChange.emit(this.loading);
        }
      },
      error: (error) => {
        console.error('Error submitting prompt:', error);
        alert('AI is not perfect, try again!');
        // ... existing error handling ...
      }
    });

  }

  onEnterKey(event: KeyboardEvent) {
    if (event.ctrlKey || event.metaKey) {
      event.preventDefault();
      this.submitPrompt();
    }
  }
}
