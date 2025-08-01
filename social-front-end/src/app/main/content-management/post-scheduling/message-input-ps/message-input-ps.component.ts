import {Component, Input} from '@angular/core';
import {FormGroup, ReactiveFormsModule} from "@angular/forms";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-message-input-ps',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule
  ],
  templateUrl: './message-input-ps.component.html',
  styleUrl: './message-input-ps.component.css'
})
export class MessageInputPsComponent {
  @Input() formGroup!: FormGroup;

  get messageControl() {
    return this.formGroup.get('message')!;
  }
}
