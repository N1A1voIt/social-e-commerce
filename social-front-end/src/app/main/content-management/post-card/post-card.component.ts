import {Component, Input} from '@angular/core';
import {MotherPostDisplay} from "../content.service";
import {DatePipe, NgIf} from "@angular/common";

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [
    NgIf,
    DatePipe
  ],
  templateUrl: './post-card.component.html',
  styleUrl: './post-card.component.css'
})
export class PostCardComponent {
  @Input() post!: MotherPostDisplay;

}
