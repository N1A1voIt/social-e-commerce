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
    // Set the selected file
    this.formGroup.patchValue({
      selectedFile: file,
      isUploading: false
    });

    // Create preview
    const reader = new FileReader();
    reader.onload = (e) => {
      this.formGroup.patchValue({
        previewUrl: e.target?.result as string
      });
    };
    reader.readAsDataURL(file);
  }

  onFileRemoved(): void {
    this.formGroup.patchValue({
      imageUrl: '',
      previewUrl: '',
      selectedFile: null
    });
  }

  // File upload is now handled by the parent component during form submission
}
