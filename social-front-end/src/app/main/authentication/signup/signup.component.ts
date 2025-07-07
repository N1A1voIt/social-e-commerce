import { Component } from '@angular/core';
import {LoginInputComponent} from "../../../shared/forms/auth/login-input/login-input.component";
import {PlatformButtonComponent} from "../../../shared/forms/auth/platform-button/platform-button.component";

@Component({
  selector: 'app-signup',
  standalone: true,
    imports: [
        LoginInputComponent,
        PlatformButtonComponent
    ],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {

}
