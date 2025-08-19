import { Component } from '@angular/core';
import {FormContainerComponent} from "../../../shared/form-container/form-container.component";
import {BeautifulButtonComponent} from "../../../shared/beautiful-button/beautiful-button.component";

@Component({
  selector: 'app-inbox-popup',
  standalone: true,
  imports: [
    FormContainerComponent,
    BeautifulButtonComponent
  ],
  templateUrl: './inbox-popup.component.html',
  styleUrl: './inbox-popup.component.css'
})
export class InboxPopupComponent {
  isLoading = false;

}
