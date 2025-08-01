import {Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormGroup} from "@angular/forms";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-file-upload',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './file-upload.component.html',
  styleUrl: './file-upload.component.css'
})
export class FileUploadComponent {
  @Input() formGroup!: FormGroup;
  @Output() fileSelected = new EventEmitter<File>();
  @Output() fileRemoved = new EventEmitter<void>();

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  uploadError = '';

  get previewUrl(): string {
    return this.formGroup.get('previewUrl')?.value || '';
  }

  get imageUrl(): string {
    return this.formGroup.get('imageUrl')?.value || '';
  }

  get isUploading(): boolean {
    return this.formGroup.get('isUploading')?.value || false;
  }

  onFileInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      if (this.validateFile(file)) {
        this.fileSelected.emit(file);
      }
    }
  }

  onRemoveFile(event: Event): void {
    event.stopPropagation();
    this.fileRemoved.emit();
    // Reset file input
    this.fileInput.nativeElement.value = '';
  }

  private validateFile(file: File): boolean {
    const maxSize = 10 * 1024 * 1024; // 10MB
    const allowedTypes = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];

    if (file.size > maxSize) {
      this.uploadError = 'File size must be less than 10MB';
      return false;
    }

    if (!allowedTypes.includes(file.type)) {
      this.uploadError = 'Only PNG, JPG, GIF, and WebP files are allowed';
      return false;
    }

    this.uploadError = '';
    return true;
  }
}
