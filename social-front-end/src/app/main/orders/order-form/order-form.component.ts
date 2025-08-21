import {Component, Input} from '@angular/core';
import {FormContainerComponent} from "../../../shared/form-container/form-container.component";

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [
    FormContainerComponent
  ],
  templateUrl: './order-form.component.html',
  styleUrl: './order-form.component.css'
})
export class OrderFormComponent {
  @Input() isVisible: boolean = false;
  @Input() order:any;
}
