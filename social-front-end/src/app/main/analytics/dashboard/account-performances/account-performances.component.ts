import {Component, Input} from '@angular/core';
import {DecimalPipe} from "@angular/common";

@Component({
  selector: 'app-account-performances',
  standalone: true,
  imports: [
    DecimalPipe
  ],
  templateUrl: './account-performances.component.html',
  styleUrl: './account-performances.component.css'
})
export class AccountPerformancesComponent {
  @Input() percentage!:number;
  @Input() amount!:number;
}
