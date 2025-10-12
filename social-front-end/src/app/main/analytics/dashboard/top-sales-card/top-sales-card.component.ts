import {Component, Input} from '@angular/core';
import {DashboardStats} from "../dashboard.service";

@Component({
  selector: 'app-top-sales-card',
  standalone: true,
  imports: [],
  templateUrl: './top-sales-card.component.html',
  styleUrl: './top-sales-card.component.css'
})
export class TopSalesCardComponent {
  @Input() dashboardStats!: DashboardStats | null;
}
