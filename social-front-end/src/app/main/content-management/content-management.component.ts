import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {SelectResearchComponent} from "./select-research/select-research.component";
import {PostCardComponent} from "./post-card/post-card.component";
import {FormContainerComponent} from "../../shared/form-container/form-container.component";
import {ContentService, MotherPostDisplay, PostChild} from "./content.service";
import {error} from "@angular/compiler-cli/src/transformers/util";
import {NgForOf, NgIf} from "@angular/common";
import {PostSchedulingComponent} from "./post-scheduling/post-scheduling.component";

@Component({
  selector: 'app-content-management',
  standalone: true,
  imports: [
    SelectResearchComponent,
    PostCardComponent,
    FormContainerComponent,
    NgForOf,
    PostSchedulingComponent,
    NgIf
  ],
  templateUrl: './content-management.component.html',
  styleUrl: './content-management.component.css'
})
export class ContentManagementComponent implements OnInit{
  posts : MotherPostDisplay[] = [];
  showForm = false;

  constructor(
    private contentService: ContentService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.fetchPosts();
  }
  fetchPosts() {
    this.posts = [];
    this.contentService.fetchContent().subscribe({
      next: (response) => {
        console.log(response)
        this.posts = response;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  onViewPostDetails(post: MotherPostDisplay) {
    this.router.navigate(['/basic/content/post', post.idPost]);
  }
}
