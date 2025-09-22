import {Component, OnInit} from '@angular/core';
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {Delivery} from "../../models/delivery.model";
import {DeliveryService} from "../../services/delivery.service";
import {MissionHistory} from "../../models/previous-mission.model";
import {MissionHistoryService} from "../../services/mission-history.service";

@Component({
  selector: 'app-previous-mission',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    NgClass
  ],
  templateUrl: './previous-mission.component.html',
  styleUrl: './previous-mission.component.css'
})
export class PreviousMissionComponent implements OnInit {
  missions: MissionHistory[] = [];
  loading: boolean = true;
  error: string | null = null;

  constructor(private missionService: MissionHistoryService) {}

  ngOnInit(): void {
    this.loadMissions();
  }

  loadMissions(): void {
    this.loading = true;
    this.error = null;

    this.missionService.getPreviousMissions().subscribe({
      next: (response) => {
        this.missions = response.data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching missions:', err.error.errors[0].message);
        this.error = 'Failed to load missions. Please try again later.';
        this.loading = false;
      }
    });
  }

  formatDate(date: any): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleString();
  }

  getStatusClass(status: string): string {
    // This method returns Tailwind CSS classes for immediate styling
    // The HTML template also adds our custom CSS classes (status-pending, etc.)
    switch (status.toLowerCase()) {
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'in_progress':
      case 'in progress':
        return 'bg-blue-100 text-blue-800';
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'cancelled':
      case 'canceled':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}
