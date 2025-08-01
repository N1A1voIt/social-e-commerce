import {Component, OnInit} from '@angular/core';
import {ContentService} from "../content.service";
import {ManagedPageCPL} from "../../settings/account-details/account-details.component";
import {JsonPipe, NgForOf, NgIf} from "@angular/common";
import {PlatformRowComponent} from "../../settings/managed-account/platform-row/platform-row.component";
import {PlatformPostCheckComponent} from "../platform-post-check/platform-post-check.component";
import {Product} from "../../products/products.types";
import {ProductRowComponent} from "../product-row/product-row.component";
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {HttpClient} from "@angular/common/http";
import {MainMessageComponent} from "./main-message/main-message.component";
import {MediaDetailsContainerComponent} from "./media-details-container/media-details-container.component";
import {SubmitButtonComponent} from "./submit-button/submit-button.component";
import {FormValidationSummaryComponent} from "./form-validation-summary/form-validation-summary.component";
interface MediaDetail {
  imageUrl: string;
  message: string;
}

interface PostData {
  pagesIds: { id: string; platform: string }[];
  mediaDetails: MediaDetail[];
  mainMessage: string;
  idProducts: number[];
}
@Component({
  selector: 'app-post-scheduling',
  standalone: true,
  imports: [
    NgForOf,
    PlatformRowComponent,
    PlatformPostCheckComponent,
    ProductRowComponent,
    NgIf,
    ReactiveFormsModule,
    JsonPipe,
    MainMessageComponent,
    MediaDetailsContainerComponent,
    SubmitButtonComponent,
    FormValidationSummaryComponent
  ],
  templateUrl: './post-scheduling.component.html',
  styleUrl: './post-scheduling.component.css'
})
export class PostSchedulingComponent implements OnInit{
  pages:ManagedPageCPL[] = [];
  products:Product[] = [];
  step:string = 'platforms';
  postForm: FormGroup;
  isSubmitting = false;
  uploadError = '';
  constructor(private postService: ContentService ,private fb: FormBuilder,
              private http: HttpClient
  ) {
    this.postForm = this.createForm();
  }

  createForm(): FormGroup {
    return this.fb.group({
      mainMessage: ['', [Validators.required, Validators.maxLength(500)]],
      mediaDetails: this.fb.array([], Validators.required)
    });
  }

  get mediaDetailsArray(): FormArray {
    return this.postForm.get('mediaDetails') as FormArray;
  }

  addMediaDetail(): void {
    const mediaGroup = this.fb.group({
      imageUrl: ['', Validators.required],
      message: ['', [Validators.required, Validators.maxLength(200)]],
      previewUrl: [''],
      selectedFile: [null],
      isUploading: [false]
    });
    this.mediaDetailsArray.push(mediaGroup);
  }

  removeMediaDetail(index: number): void {
    if (this.mediaDetailsArray.length > 1) {
      this.mediaDetailsArray.removeAt(index);
    }
  }

  onSubmit(): void {
    if (this.postForm.valid) {
      this.isSubmitting = true;
      const postData = this.buildPostData();

      console.log('Post Data:', postData);

      // Replace with your actual API endpoint
      this.http.post('/api/social-posts', postData).subscribe({
        next: (response) => {
          console.log('Post created successfully:', response);
          this.isSubmitting = false;
          // Handle success (redirect, show message, etc.)
        },
        error: (error) => {
          console.error('Failed to create post:', error);
          this.isSubmitting = false;
          // Handle error
        }
      });
    } else {
      // Mark all fields as touched to show validation errors
      this.markFormGroupTouched(this.postForm);
    }
  }

  markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(field => {
      const control = formGroup.get(field);
      control?.markAsTouched({ onlySelf: true });

      if (control instanceof FormArray) {
        control.controls.forEach(arrayControl => {
          if (arrayControl instanceof FormGroup) {
            this.markFormGroupTouched(arrayControl);
          }
        });
      }
    });
  }

  buildPostData(): PostData {
    const formValue = this.postForm.value;

    return {
      pagesIds: [
        { id: "757064984155341", platform: "facebook" }
      ],
      mediaDetails: formValue.mediaDetails.map((media: any) => ({
        imageUrl: media.imageUrl,
        message: media.message
      })),
      mainMessage: formValue.mainMessage,
      idProducts: [101, 102, 103] // Static for now
    };
  }
    ngOnInit(): void {
        this.addMediaDetail();
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
