import { Component } from '@angular/core';
import {SelectResearchComponent} from "./select-research/select-research.component";
import {PostCardComponent} from "./post-card/post-card.component";

@Component({
  selector: 'app-content-management',
  standalone: true,
  imports: [
    SelectResearchComponent,
    PostCardComponent
  ],
  templateUrl: './content-management.component.html',
  styleUrl: './content-management.component.css'
})
export class ContentManagementComponent {

}
