import { Component } from '@angular/core';
import {LoginInputComponent} from "../../../shared/forms/auth/login-input/login-input.component";
import {PlatformButtonComponent} from "../../../shared/forms/auth/platform-button/platform-button.component";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    LoginInputComponent,
    PlatformButtonComponent
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

}
