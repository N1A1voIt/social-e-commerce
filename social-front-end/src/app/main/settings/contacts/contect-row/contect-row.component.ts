import { Component } from '@angular/core';
import {CheckboxComponent} from "../../../../shared/checkbox/checkbox.component";

@Component({
  selector: 'app-contect-row',
  standalone: true,
  imports: [
    CheckboxComponent
  ],
  templateUrl: './contect-row.component.html',
  styleUrl: './contect-row.component.css'
})
export class ContectRowComponent {

}
