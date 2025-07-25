import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-basic-button',
  standalone: true,
  imports: [],
  templateUrl: './basic-button.component.html',
  styleUrl: './basic-button.component.css'
})
export class BasicButtonComponent {
  @Input() disabled!: boolean;
  @Input() loading!: boolean;

}
