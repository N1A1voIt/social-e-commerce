import {Component, Input} from '@angular/core';
import {CheckboxComponent} from "../../../shared/checkbox/checkbox.component";

@Component({
  selector: 'app-platform-post-check',
  standalone: true,
    imports: [
        CheckboxComponent
    ],
  templateUrl: './platform-post-check.component.html',
  styleUrl: './platform-post-check.component.css'
})
export class PlatformPostCheckComponent {
  @Input() platform!: string;
  @Input() pageTitle!: string;
  @Input() username!: string;
  @Input() logo!: string;  // Use to dynamically set the image
  @Input() status!: string;
  @Input() associatedMedia!: string;
  @Input() linkToPlatform!: string;
}
