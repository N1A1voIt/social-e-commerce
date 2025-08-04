import {Component, Input} from '@angular/core';
import {AbstractControl, FormControl, ReactiveFormsModule} from "@angular/forms";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-main-message',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf
  ],
  templateUrl: './main-message.component.html',
  styleUrl: './main-message.component.css'
})
export class MainMessageComponent {
  @Input() control!: FormControl;
}
