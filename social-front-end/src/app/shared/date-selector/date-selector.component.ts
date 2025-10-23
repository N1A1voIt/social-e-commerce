import { Component, EventEmitter, Input, OnInit, OnDestroy, Output, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import flatpickr from 'flatpickr';
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-date-selector',
  standalone: true,
  templateUrl: "date-selector.component.html",
  styleUrl: "date-selector.component.css",
  imports: [
    NgIf
  ]
})
export class DateSelectorComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input() startDate: Date = new Date();
  @Input() endDate: Date = new Date();
  @Input() placeholder: string = 'Select date range';
  @Output() dateRangeChange = new EventEmitter<{startDate: Date, endDate: Date}>();

  @ViewChild('dateRangeDisplay', { static: false }) dateRangeDisplay!: ElementRef<HTMLDivElement>;

  formattedStart = '';
  formattedEnd = '';
  private flatpickrInstance: flatpickr.Instance | null = null;
  private isInitialized = false;

  ngOnInit() {
    // Initialize with default dates if not provided
    if (!this.startDate) {
      this.startDate = new Date();
    }
    if (!this.endDate) {
      // Set end date to 30 days after start date
      this.endDate = new Date(this.startDate.getTime() + (30 * 24 * 60 * 60 * 1000));
    }

    this.formattedStart = this.formatDate(this.startDate);
    this.formattedEnd = this.formatDate(this.endDate);
  }

  ngAfterViewInit() {
    // Initialize flatpickr after view is ready
    this.initializeFlatpickr();
  }

  ngOnDestroy() {
    if (this.flatpickrInstance) {
      this.flatpickrInstance.destroy();
    }
  }

  formatDate(date: Date): string {
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  private initializeFlatpickr() {
    if (!this.dateRangeDisplay?.nativeElement || this.isInitialized) {
      return;
    }

    this.flatpickrInstance = flatpickr(this.dateRangeDisplay.nativeElement, {
      mode: 'range',
      dateFormat: 'M j, Y',
      defaultDate: [this.startDate, this.endDate],
      allowInput: false,
      clickOpens: false, // We'll handle opening manually
      onClose: (selectedDates: Date[]) => {
        if (selectedDates.length === 2) {
          this.startDate = selectedDates[0];
          this.endDate = selectedDates[1];
          this.formattedStart = this.formatDate(this.startDate);
          this.formattedEnd = this.formatDate(this.endDate);

          // Emit the date change event
          this.dateRangeChange.emit({
            startDate: this.startDate,
            endDate: this.endDate
          });
        }
      },
    });

    this.isInitialized = true;
  }

  openDatePicker() {
    if (!this.flatpickrInstance) {
      this.initializeFlatpickr();
    }

    if (this.flatpickrInstance) {
      this.flatpickrInstance.open();
    }
  }

  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.openDatePicker();
    }
  }
}
