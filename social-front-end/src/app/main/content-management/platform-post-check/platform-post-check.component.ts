import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CheckboxComponent} from "../../../shared/checkbox/checkbox.component";

@Component({
  selector: 'app-platform-post-check',
  standalone: true,
    imports: [
        CheckboxComponent
    ],
  templateUrl: './platform-post-check.component.html',
  styleUrl: './platform-post-check.component.css'
})
export class PlatformPostCheckComponent {
  @Input() platform!: string;
  @Input() pageTitle!: string;
  @Input() username!: string;
  @Input() logo!: string;
  @Input() status: boolean = false;
  @Input() associatedMedia!: string;
  @Input() linkToPlatform!: string;
  @Input() id!: string;

  get state() {
    if(this.status){
      return 'active';
    } else {
      return 'unactive';
    }
  }

  @Output() statusChange = new EventEmitter<any>();

  updateStatus() {
    this.status = !this.status;
    const data = {
      id: this.id,
      platform: this.platform
    };
    this.statusChange.emit(data);
  }
}
