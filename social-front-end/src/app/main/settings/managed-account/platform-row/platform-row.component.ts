import {Component, Input} from '@angular/core';
import {CheckboxComponent} from "../../../../shared/checkbox/checkbox.component";

@Component({
  selector: 'app-platform-row',
  standalone: true,
  imports: [
    CheckboxComponent
  ],
  templateUrl: './platform-row.component.html',
  styleUrl: './platform-row.component.css'
})
export class PlatformRowComponent {
  @Input() platform!: string;
  @Input() pageTitle!: string;
  @Input() username!: string;
  @Input() logo!: string;  // Use to dynamically set the image
  @Input() status!: string;
  @Input() associatedMedia!: string;
  @Input() linkToPlatform!: string;
}
