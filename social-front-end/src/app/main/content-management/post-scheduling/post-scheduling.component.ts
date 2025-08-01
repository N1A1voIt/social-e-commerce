import {Component, OnInit} from '@angular/core';
import {ContentService} from "../content.service";
import {ManagedPageCPL} from "../../settings/account-details/account-details.component";
import {NgForOf} from "@angular/common";
import {PlatformRowComponent} from "../../settings/managed-account/platform-row/platform-row.component";
import {PlatformPostCheckComponent} from "../platform-post-check/platform-post-check.component";

@Component({
  selector: 'app-post-scheduling',
  standalone: true,
  imports: [
    NgForOf,
    PlatformRowComponent,
    PlatformPostCheckComponent
  ],
  templateUrl: './post-scheduling.component.html',
  styleUrl: './post-scheduling.component.css'
})
export class PostSchedulingComponent implements OnInit{
  pages:ManagedPageCPL[] = [];
  constructor(private postService: ContentService) {
  }

  ngOnInit(): void {
      this.postService.fetchManagedPages().subscribe({
        next: (data) => {
          this.pages = data;
        },
        error: (err) => {
          console.log(err.message);
          console.error('Failed to load managed pages', err);
        },
      });
  }
}
