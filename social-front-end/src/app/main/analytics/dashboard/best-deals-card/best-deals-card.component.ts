import {Component, Input} from '@angular/core';
import {DashboardStats} from "../dashboard.service";
import {DecimalPipe} from "@angular/common";
import {FrenchNumberPipe} from "../../../../shared/french-number.pipe";

@Component({
  selector: 'app-best-deals-card',
  standalone: true,
  imports: [
    DecimalPipe,
    FrenchNumberPipe
  ],
  templateUrl: './best-deals-card.component.html',
  styleUrl: './best-deals-card.component.css'
})
export class BestDealsCardComponent {
  @Input() dashboardStats!: DashboardStats | null;
}
