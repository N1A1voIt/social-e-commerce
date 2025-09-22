import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ContactsComponent} from "../../main/settings/contacts/contacts.component";
import {ManagedAccountComponent} from "../../main/settings/managed-account/managed-account.component";
import {NgIcon} from "@ng-icons/core";
import {LoginInputComponent} from "../forms/auth/login-input/login-input.component";
import {BasicInputComponent} from "../basic-input/basic-input.component";
import {BasicButtonComponent} from "../basic-button/basic-button.component";
import {NgClass} from "@angular/common";
import { trigger, state, style, transition, animate, query, stagger } from '@angular/animations';
@Component({
  selector: 'app-form-container',
  standalone: true,
  imports: [
    ContactsComponent,
    ManagedAccountComponent,
    NgIcon,
    LoginInputComponent,
    BasicInputComponent,
    BasicButtonComponent,
    NgClass
  ],
  animations: [
    // Backdrop fade animation
    trigger('backdropAnimation', [
      state('hidden', style({
        opacity: 0,
        backdropFilter: 'blur(0px)'
      })),
      state('visible', style({
        opacity: 1,
        backdropFilter: 'blur(4px)'
      })),
      transition('hidden => visible', [
        animate('300ms ease-in-out')
      ]),
      transition('visible => hidden', [
        animate('200ms ease-in-out')
      ])
    ]),

    // Slide in from right animation
    trigger('slideInAnimation', [
      state('hidden', style({
        transform: 'translateX(100%)',
        boxShadow: '0 0 0 rgba(0,0,0,0)'
      })),
      state('visible', style({
        transform: 'translateX(0)',
        boxShadow: '-10px 0 30px rgba(0,0,0,0.1)'
      })),
      transition('hidden => visible', [
        animate('400ms cubic-bezier(0.25, 0.8, 0.25, 1)')
      ]),
      transition('visible => hidden', [
        animate('300ms cubic-bezier(0.55, 0, 0.55, 0.2)')
      ])
    ]),

    // Header animation
    trigger('headerAnimation', [
      state('hidden', style({
        opacity: 0,
        transform: 'translateY(-20px)'
      })),
      state('visible', style({
        opacity: 1,
        transform: 'translateY(0)'
      })),
      transition('hidden => visible', [
        animate('500ms 200ms cubic-bezier(0.35, 0, 0.25, 1)')
      ]),
      transition('visible => hidden', [
        animate('200ms cubic-bezier(0.55, 0, 0.55, 0.2)')
      ])
    ]),

    // Content animation
    trigger('contentAnimation', [
      state('hidden', style({
        opacity: 0,
        transform: 'translateY(30px)'
      })),
      state('visible', style({
        opacity: 1,
        transform: 'translateY(0)'
      })),
      transition('hidden => visible', [
        animate('600ms 400ms cubic-bezier(0.35, 0, 0.25, 1)')
      ]),
      transition('visible => hidden', [
        animate('200ms cubic-bezier(0.55, 0, 0.55, 0.2)')
      ])
    ]),

    // Alternative slide from right
    trigger('slideFromRight', [
      transition(':enter', [
        style({ transform: 'translateX(100%)' }),
        animate('400ms cubic-bezier(0.25, 0.8, 0.25, 1)',
          style({ transform: 'translateX(0)' }))
      ]),
      transition(':leave', [
        animate('300ms cubic-bezier(0.55, 0, 0.55, 0.2)',
          style({ transform: 'translateX(100%)' }))
      ])
    ]),

    // Backdrop fade
    trigger('backdropFade', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('300ms ease-in-out', style({ opacity: 1 }))
      ]),
      transition(':leave', [
        animate('200ms ease-in-out', style({ opacity: 0 }))
      ])
    ]),

    // Fade in up animation
    trigger('fadeInUp', [
      transition(':enter', [
        style({
          opacity: 0,
          transform: 'translateY(20px)'
        }),
        animate('{{ delay }} 500ms cubic-bezier(0.35, 0, 0.25, 1)',
          style({
            opacity: 1,
            transform: 'translateY(0)'
          }))
      ])
    ]),

    // Stagger children animation
    trigger('staggerChildren', [
      transition('* => *', [
        query(':enter', [
          style({ opacity: 0, transform: 'translateY(30px)' }),
          stagger(100, [
            animate('500ms cubic-bezier(0.35, 0, 0.25, 1)',
              style({ opacity: 1, transform: 'translateY(0)' }))
          ])
        ], { optional: true })
      ])
    ])
  ],
  templateUrl: './form-container.component.html',
  styleUrl: './form-container.component.css'
})
export class FormContainerComponent {
  @Input() label:string = 'New Product form';
  @Input() isVisible: boolean = false;
  @Output() closeForm = new EventEmitter<void>();
  closeFormHandler() {
    this.closeForm.emit();
  }
}
