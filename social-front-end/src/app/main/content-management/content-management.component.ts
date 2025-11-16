import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {SelectResearchComponent} from "./select-research/select-research.component";
import {PostCardComponent} from "./post-card/post-card.component";
import {FormContainerComponent} from "../../shared/form-container/form-container.component";
import {ContentService, MotherPostDisplay, PostChild} from "./content.service";
import {error} from "@angular/compiler-cli/src/transformers/util";
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {PostSchedulingComponent} from "./post-scheduling/post-scheduling.component";
import {PaginatorModule} from "primeng/paginator";
import {BasicInputComponent} from "../../shared/basic-input/basic-input.component";
import {BasicButtonComponent} from "../../shared/basic-button/basic-button.component";
import {DropdownModule} from "primeng/dropdown";
import {FormsModule} from "@angular/forms";

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
    NgClass,
    PaginatorModule,
    BasicInputComponent,
    BasicButtonComponent,
    DropdownModule,
    FormsModule
  ],
  templateUrl: './content-management.component.html',
  styleUrl: './content-management.component.css'
})
export class ContentManagementComponent implements OnInit{
  posts : MotherPostDisplay[] = [];
  showForm = false;
  loading = false;

  // Pagination properties
  currentPage: number = 0;
  pageSize: number = 10;
  totalRecords: number = 0;

  // Filter properties
  filterTitle: string = '';
  filterType: string | null = null;
  filterStartDate: Date | null = null;
  filterEndDate: Date | null = null;

  typeOptions = [
    { label: 'All Types', value: null },
    { label: 'Facebook', value: 'facebook_post' },
    { label: 'Instagram', value: 'instagram_post' }
  ];

  constructor(
    private contentService: ContentService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.fetchPosts();
  }

  fetchPosts(page?: number) {
    this.loading = true;
    const pageToFetch = page !== undefined ? page : this.currentPage;

    const filters: any = {};

    if (this.filterTitle && this.filterTitle.trim()) {
      filters.title = this.filterTitle.trim();
    }

    if (this.filterType !== null) {
      filters.type = this.filterType;
    }

    let startDateStr = null;
    let endDateStr = null;

    if (this.filterStartDate) {
      const startDate = new Date(this.filterStartDate);
      startDate.setHours(0, 0, 0, 0);
      startDateStr = this.formatDateToISO(startDate);
    }

    if (this.filterEndDate) {
      const endDate = new Date(this.filterEndDate);
      endDate.setHours(23, 59, 59, 999);
      endDateStr = this.formatDateToISO(endDate);
    }

    if (startDateStr) {
      filters.startDate = startDateStr;
    }

    if (endDateStr) {
      filters.endDate = endDateStr;
    }

    this.contentService.fetchContent(pageToFetch, this.pageSize, filters).subscribe({
      next: (response) => {
        console.log(response)
        this.posts = response.posts;
        this.totalRecords = response.totalPosts;
        this.loading = false;
      },
      error: (err) => {
        console.log(err);
        this.loading = false;
      }
    })
  }

  formatDateToISO(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  }

  onPageChange(event: any) {
    this.currentPage = event.page;
    this.pageSize = event.rows;
    this.fetchPosts(this.currentPage);
  }

  applyFilters() {
    this.currentPage = 0;
    this.fetchPosts(0);
  }

  clearFilters() {
    this.filterTitle = '';
    this.filterType = null;
    this.filterStartDate = null;
    this.filterEndDate = null;
    this.currentPage = 0;
    this.fetchPosts(0);
  }

  onViewPostDetails(post: MotherPostDisplay) {
    this.router.navigate(['/basic/content/post', post.idPost]);
  }
}
