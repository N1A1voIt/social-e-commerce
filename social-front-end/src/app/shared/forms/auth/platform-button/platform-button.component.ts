import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-platform-button',
  standalone: true,
  imports: [],
  templateUrl: './platform-button.component.html',
  styleUrl: './platform-button.component.css'
})
export class PlatformButtonComponent {
  @Input() logoName!:string;
}
