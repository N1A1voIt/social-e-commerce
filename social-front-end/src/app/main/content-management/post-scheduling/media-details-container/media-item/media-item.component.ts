import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormGroup, ReactiveFormsModule} from "@angular/forms";
import {HttpClient} from "@angular/common/http";
import {FileUploadComponent} from "../../../../../shared/file-upload/file-upload.component";
import {MessageInputPsComponent} from "../../message-input-ps/message-input-ps.component";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-media-item',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FileUploadComponent,
    MessageInputPsComponent,
    NgIf
  ],
  templateUrl: './media-item.component.html',
  styleUrl: './media-item.component.css'
})
export class MediaItemComponent {
  @Input() formGroup!: FormGroup;
  @Input() index!: number;
  @Input() canRemove!: boolean;
  @Output() remove = new EventEmitter<number>();

  constructor(private http: HttpClient) {}

  onRemove(): void {
    this.remove.emit(this.index);
  }

  onFileSelected(file: File): void {
    // Set loading state
    this.formGroup.patchValue({
      selectedFile: file,
      isUploading: true
    });

    // Create preview
    const reader = new FileReader();
    reader.onload = (e) => {
      this.formGroup.patchValue({
        previewUrl: e.target?.result as string
      });
    };
    reader.readAsDataURL(file);

    // Upload file
    this.uploadFile(file);
  }

  onFileRemoved(): void {
    this.formGroup.patchValue({
      imageUrl: '',
      previewUrl: '',
      selectedFile: null
    });
  }

  private uploadFile(file: File): void {
    const formData = new FormData();
    formData.append('file', file);

    // Replace with your actual upload endpoint
    this.http.post<{url: string}>('/api/upload', formData).subscribe({
      next: (response) => {
        this.formGroup.patchValue({
          imageUrl: response.url,
          isUploading: false
        });
      },
      error: (error) => {
        console.error('Upload failed:', error);
        this.formGroup.patchValue({
          isUploading: false
        });
      }
    });
  }
}
