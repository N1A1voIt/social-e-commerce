import { Component } from '@angular/core';
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

}
