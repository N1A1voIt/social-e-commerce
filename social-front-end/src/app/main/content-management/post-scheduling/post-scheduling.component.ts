import {Component, OnInit} from '@angular/core';
import {ContentService} from "../content.service";
import {ManagedPageCPL} from "../../settings/account-details/account-details.component";
import {NgForOf} from "@angular/common";
import {PlatformRowComponent} from "../../settings/managed-account/platform-row/platform-row.component";
import {PlatformPostCheckComponent} from "../platform-post-check/platform-post-check.component";
import {Product} from "../../products/products.types";
import {ProductRowComponent} from "../product-row/product-row.component";

@Component({
  selector: 'app-post-scheduling',
  standalone: true,
  imports: [
    NgForOf,
    PlatformRowComponent,
    PlatformPostCheckComponent,
    ProductRowComponent
  ],
  templateUrl: './post-scheduling.component.html',
  styleUrl: './post-scheduling.component.css'
})
export class PostSchedulingComponent implements OnInit{
  pages:ManagedPageCPL[] = [];
  products:Product[] = [];
  constructor(private postService: ContentService) {
  }

  ngOnInit(): void {
      this.postService.fetchUtilities().subscribe({
        next: (data) => {
          this.pages = data.managedPages;
          this.products = data.products;
        },
        error: (err) => {
          console.log(err.message);
          console.error('Failed to load managed pages', err);
        },
      });
  }
}
