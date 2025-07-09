import {Component, Input} from '@angular/core';
import {NgClass} from "@angular/common";

@Component({
  selector: 'app-descriptive-card',
  standalone: true,
  imports: [
    NgClass
  ],
  templateUrl: './descriptive-card.component.html',
  styleUrl: './descriptive-card.component.css'
})
export class DescriptiveCardComponent {
  @Input() isActive:boolean = false;
}
