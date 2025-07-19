import { Component } from '@angular/core';
import {BasicInputComponent} from "../../../shared/basic-input/basic-input.component";
import {NgIcon} from "@ng-icons/core";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-profile-edit-form',
  standalone: true,
  imports: [
    BasicInputComponent,
    NgIcon,
    NgIf
  ],
  templateUrl: './profile-edit-form.component.html',
  styleUrl: './profile-edit-form.component.css'
})
export class ProfileEditFormComponent {

}
