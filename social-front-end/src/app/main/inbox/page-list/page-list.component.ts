import {Component, Input} from '@angular/core';
import {CheckboxComponent} from "../../../shared/checkbox/checkbox.component";
import {ManagedPageCPL} from "../../settings/account-details/account-details.component";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-page-list',
  standalone: true,
  imports: [
    CheckboxComponent,
    NgForOf
  ],
  templateUrl: './page-list.component.html',
  styleUrl: './page-list.component.css'
})
export class PageListComponent {
  @Input() pages: ManagedPageCPL[] = [];
}
