import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {SelectResearchComponent} from "./select-research/select-research.component";
import {PostCardComponent} from "./post-card/post-card.component";
import {FormContainerComponent} from "../../shared/form-container/form-container.component";
import {ContentService, MotherPostDisplay, PostChild} from "./content.service";
import {error} from "@angular/compiler-cli/src/transformers/util";
import {NgForOf, NgIf} from "@angular/common";
import {PostSchedulingComponent} from "./post-scheduling/post-scheduling.component";
import {PaginatorModule} from "primeng/paginator";

@Component({
  selector: 'app-content-management',
  standalone: true,
  imports: [
    SelectResearchComponent,
    PostCardComponent,
    FormContainerComponent,
    NgForOf,
    PostSchedulingComponent,
    NgIf,
    PaginatorModule
  ],
  templateUrl: './content-management.component.html',
  styleUrl: './content-management.component.css'
})
export class ContentManagementComponent implements OnInit{
  posts : MotherPostDisplay[] = [];
  allPosts: MotherPostDisplay[] = [];
  showForm = false;

  // Pagination properties
  currentPage: number = 0;
  itemsPerPage: number = 10;
  totalRecords: number = 0;

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
        this.allPosts = response;
        this.totalRecords = response.length;
        this.updateDisplayedPosts();
      },
      error: (err) => {
        console.log(err);
      }
    })
  }

  updateDisplayedPosts() {
    const startIndex = this.currentPage * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.posts = this.allPosts.slice(startIndex, endIndex);
  }

  onPageChange(event: any) {
    this.currentPage = event.page;
    this.itemsPerPage = event.rows;
    this.updateDisplayedPosts();
  }

  onViewPostDetails(post: MotherPostDisplay) {
    this.router.navigate(['/basic/content/post', post.idPost]);
  }
}
