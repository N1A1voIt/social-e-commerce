import {ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ContentService} from "../content.service";
import {ManagedPageCPL} from "../../settings/account-details/account-details.component";
import {JsonPipe, NgForOf, NgIf} from "@angular/common";
import {PlatformRowComponent} from "../../settings/managed-account/platform-row/platform-row.component";
import {PlatformPostCheckComponent} from "../platform-post-check/platform-post-check.component";
import {Product} from "../../products/products.types";
import {ProductRowComponent} from "../product-row/product-row.component";
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from "@angular/forms";
import {HttpClient} from "@angular/common/http";
import {MainMessageComponent} from "./main-message/main-message.component";
import {MediaDetailsContainerComponent} from "./media-details-container/media-details-container.component";
import {SubmitButtonComponent} from "./submit-button/submit-button.component";
import {FormValidationSummaryComponent} from "./form-validation-summary/form-validation-summary.component";
import {SupabaseService} from "../../../shared/supabase.service";
import {PromptFormComponent} from "./prompt-form/prompt-form.component";
import {javaHost} from "../../../../environments/environment";
import {ManagedPage} from "../../authentication/validate-pages/page.service";
import {firstValueFrom} from "rxjs";
interface MediaDetail {
  imageUrl: string;
  message: string;
}

interface PostData {
  pagesIds: { pageId: string; platform: string }[];
  mediaDetails: MediaDetail[];
  mainMessage: string;
  idProducts: number[];
  scheduledUnixTime?: number; // if set, schedule instead of immediate publish
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
    FormValidationSummaryComponent,
    FormsModule,
    PromptFormComponent
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
  // pagesIn : Map<string,ManagedPageCPL> = new Map();
  postData : PostData = {
    pagesIds: [],
    mediaDetails: [],
    mainMessage: '',
    idProducts: []
  };
  pagesIn: ManagedPageCPL[] = [];
  @Input() showForm: boolean = false;
  @Output() showFormChange = new EventEmitter<boolean>();
  loading = false;

  onLoadingChange(newValue: boolean) {
    this.loading = newValue;
  }
  onShowFormChange(newValue: boolean) {
    this.showForm = newValue;
    this.showFormChange.emit(this.showForm);
  }

  uploadError = '';
  constructor(private postService: ContentService ,private fb: FormBuilder,
              private http: HttpClient,private supabaseService:SupabaseService,private cdr: ChangeDetectorRef
  ) {
    this.postForm = this.createForm();
  }

  createForm(): FormGroup {
    return this.fb.group({
      mainMessage: ['', [Validators.required, Validators.maxLength(500)]],
      mediaDetails: this.fb.array([], Validators.required),
      scheduledEnabled: [false],
      scheduledAt: [''] // HTML datetime-local string
    });
  }
  get mainMessageControl(): FormControl {
    return this.postForm.get('mainMessage') as FormControl;
  }

  get mediaDetailsArray(): FormArray {
    return this.postForm.get('mediaDetails') as FormArray;
  }

  addMediaDetail(): void {
    const mediaGroup = this.fb.group({
      imageUrl: [''],
      message: ['', [Validators.maxLength(200)]],
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
  isPageSelected(page: ManagedPageCPL): boolean {
    return this.pagesIn.some(p => p.platformIdentifier === page.platformIdentifier);
  }
  async onSubmit(): Promise<void> {

    if (this.postForm.valid) {
      this.isSubmitting = true;
      const header = {
        'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
      };
      try {
        await this.uploadAllMedia();
        await this.buildPostData();
        console.log('Pages In',this.pagesIn)
        this.postData.pagesIds = [];
        for(let page of this.pagesIn){
          this.postData.pagesIds.push({
            pageId: page.platformIdentifier,
            platform: page.platform
          })
        }

        console.log('Post Data:', this.postData);

        const endpoint = (this.postData.scheduledUnixTime && this.postData.scheduledUnixTime > 0)
          ? '/api/posts/schedule-post'
          : '/api/posts/make-post';

        this.http.post(javaHost + endpoint, this.postData,{headers:header}).subscribe({
          next: (response) => {
            console.log('Post created successfully:', response);
            this.isSubmitting = false;
          },
          error: (error) => {
            console.error('Failed to create post:', error);
            this.isSubmitting = false;
          }
        });
      } catch (error) {
        console.error('Error uploading media:', error);
        this.isSubmitting = false;
      }
    } else {
      this.markFormGroupTouched(this.postForm);
    }
  }

  private async uploadAllMedia(): Promise<void> {
    const mediaItems = this.mediaDetailsArray.controls;

    for (let i = 0; i < mediaItems.length; i++) {
      const mediaGroup = mediaItems[i];
      const file = mediaGroup.get('selectedFile')?.value;

      if (file) {
        try {
          const imageUrl = await this.uploadFile(file);
          mediaGroup.patchValue({
            imageUrl: imageUrl,
            isUploading: false
          });
        } catch (error) {
          console.error(`Failed to upload media item ${i + 1}:`, error);
          throw new Error(`Failed to upload media item ${i + 1}`);
        }
      } else if (!mediaGroup.get('imageUrl')?.value) {
        throw new Error(`Media item ${i + 1} has no file selected`);
      }
    }
  }

  private uploadFile(file: File): Promise<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.supabaseService.uploadFile(file);
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

  async buildPostData(): Promise<void> {
    const formValue = this.postForm.value;

    try {
      const pages: ManagedPage[] = await firstValueFrom(this.postService.fetchPageIds());
      const pageIds = pages.map((page) => ({
        pageId: page.platformIdentifier,
        platform: page.platform
      }));

      const postData: PostData = {
        pagesIds: [...pageIds],
        mediaDetails: formValue.mediaDetails.map((media: any) => ({
          imageUrl: media.imageUrl,
          message: media.message
        })),
        mainMessage: formValue.mainMessage,
        idProducts: [101, 102, 103]
      };

      if (formValue.scheduledEnabled && formValue.scheduledAt) {
        const ms = Date.parse(formValue.scheduledAt);
        if (!isNaN(ms)) {
          postData.scheduledUnixTime = Math.floor(ms / 1000);
        }
      }

      this.postData = postData;
    } catch (err: any) {
      console.error('Failed to load managed pages', err);
      throw err; // so onSubmit can catch it
    }
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

  updateStatus(page: ManagedPageCPL) {
    const index = this.pagesIn.findIndex(p => p.platformIdentifier === page.platformIdentifier);
    if (index > -1) {
      this.pagesIn = [...this.pagesIn.slice(0, index), ...this.pagesIn.slice(index + 1)];
      console.log(`Removed: ${page.platformIdentifier}`);
    } else {
      this.pagesIn = [...this.pagesIn, page];
      console.log(`Added: ${page.platformIdentifier}`);
    }
    console.log('Current pagesIn:', this.pagesIn.map(p => p.platformIdentifier));
    this.cdr.detectChanges();
  }



  populateForm(parsed: any) {
    this.mainMessageControl.setValue(parsed.mainMessage);

    // Clear existing mediaDetails
    this.mediaDetailsArray.clear();

    // Add media details from response
    parsed.mediaDetails.forEach((mediaItem: any) => {
      const mediaGroup = this.fb.group({
        imageUrl: [mediaItem.imageUrl],
        message: [mediaItem.message, [Validators.maxLength(200)]],
        previewUrl: [mediaItem.imageUrl],  // Optional: used for previews
        selectedFile: [null],              // No file, it's already uploaded
        isUploading: [false]
      });
      this.mediaDetailsArray.push(mediaGroup);
    });
    this.cdr.detectChanges()
  }


  protected readonly JSON = JSON;
}
