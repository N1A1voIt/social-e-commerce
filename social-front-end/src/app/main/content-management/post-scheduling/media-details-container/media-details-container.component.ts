import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormArray, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {MediaItemComponent} from "./media-item/media-item.component";

@Component({
  selector: 'app-media-details-container',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgForOf,
    MediaItemComponent,
    NgIf
  ],
  templateUrl: './media-details-container.component.html',
  styleUrl: './media-details-container.component.css'
})
export class MediaDetailsContainerComponent {
  @Input() mediaDetailsArray!: FormArray<FormGroup>;
  @Input() formGroup!: FormGroup;
  @Output() addMedia = new EventEmitter<void>();
  @Output() removeMedia = new EventEmitter<number>();

  onAddMedia(): void {
    this.addMedia.emit();
  }

  onRemoveMedia(index: number): void {
    this.removeMedia.emit(index);
  }

  protected readonly JSON = JSON;
}
