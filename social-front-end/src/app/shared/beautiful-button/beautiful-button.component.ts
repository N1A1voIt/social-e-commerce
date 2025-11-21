import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-beautiful-button',
  standalone: true,
  imports: [],
  templateUrl: './beautiful-button.component.html',
  styleUrl: './beautiful-button.component.css'
})
export class BeautifulButtonComponent {
  @Input() text: string = 'Explore All';
  @Input() disabled: boolean = false;
}
