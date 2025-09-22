import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {Auth, signInWithEmailAndPassword} from "@angular/fire/auth";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {javaHost} from "../../../../environments/environment";
import {LoginInputComponent} from "../../../shared/forms/auth/login-input/login-input.component";
import {NgIf} from "@angular/common";
import {PlatformButtonComponent} from "../../../shared/forms/auth/platform-button/platform-button.component";

@Component({
  selector: 'app-dsignin',
  standalone: true,
  imports: [
    LoginInputComponent,
    NgIf,
    PlatformButtonComponent,
    ReactiveFormsModule
  ],
  templateUrl: './dsignin.component.html',
  styleUrl: './dsignin.component.css'
})
export class DsigninComponent implements OnInit{
  loginForm!: FormGroup;
  errorMessage: string = '';

  constructor(private fb: FormBuilder, private auth: Auth, private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  login() {
    const { email, password } = this.loginForm.value;

    signInWithEmailAndPassword(this.auth, email, password)
      .then(userCredential => userCredential.user.getIdToken())
      .then(idToken => this.http.post(`${javaHost}/api/delivery/auth/signin`, { idToken }).toPromise())
      .then((response: any) => {
        // Store token in localStorage
        localStorage.setItem('token',"Bearer " +  response.data);
        this.errorMessage = ''; // Clear any previous error
        this.router.navigate(['/delivery/space/dashboard']);
      })
      .catch(error => {
        if (error.error) {
          // Handle backend error response
          if (error.error.error === 'Invalid token' && error.error.message) {
            this.errorMessage = error.error.message;
          } else if (error.error.error) {
            this.errorMessage = error.error.error;
          } else {
            this.errorMessage = 'Login failed';
          }
        } else if (error.code) {
          // Handle Firebase authentication errors
          switch (error.code) {
            case 'auth/user-not-found':
              this.errorMessage = 'User not found. Please sign up first.';
              break;
            case 'auth/wrong-password':
              this.errorMessage = 'Incorrect password. Please try again.';
              break;
            case 'auth/invalid-credential':
              this.errorMessage = 'Invalid credentials. Please check your email and password.';
              break;
            case 'auth/too-many-requests':
              this.errorMessage = 'Too many failed login attempts. Please try again later.';
              break;
            default:
              this.errorMessage = error.message || 'Login failed';
          }
        } else {
          this.errorMessage = error.message || 'Login failed';
        }
      });
  }
}
