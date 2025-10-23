import {Component, OnInit} from '@angular/core';
import {LoginInputComponent} from "../../../shared/forms/auth/login-input/login-input.component";
import {NgIf} from "@angular/common";
import {PaginatorModule} from "primeng/paginator";
import {PlatformButtonComponent} from "../../../shared/forms/auth/platform-button/platform-button.component";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {Auth, GoogleAuthProvider, signInWithEmailAndPassword, signInWithPopup} from "@angular/fire/auth";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {javaHost} from "../../../../environments/environment";

@Component({
  selector: 'app-client-login',
  standalone: true,
  imports: [
    LoginInputComponent,
    NgIf,
    PaginatorModule,
    PlatformButtonComponent,
    ReactiveFormsModule
  ],
  templateUrl: './client-login.component.html',
  styleUrl: './client-login.component.css'
})
export class ClientLoginComponent implements OnInit{
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
      .then(idToken => this.http.post(`${javaHost}/api/customer/auth/signin`, { idToken }).toPromise())
      .then((response: any) => {
        localStorage.setItem('token',"Bearer " +  response.token);
        this.errorMessage = ''; // Clear any previous error
        this.router.navigate(['/client/marketplace']);
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

  loginWithGoogle() {
    signInWithPopup(this.auth, new GoogleAuthProvider())
      .then(userCredential => userCredential.user.getIdToken())
      .then(idToken => this.http.post(`${javaHost}/api/customer/auth/signin`, { idToken }).toPromise())
      .then((response: any) => {
        // Store token in localStorage
        localStorage.setItem('token',"Bearer " +  response.token);
        this.errorMessage = ''; // Clear any previous error
        this.router.navigate(['/client/marketplace']);
      })
      .catch(error => {
        if (error.error) {
          // Handle backend error response
          if (error.error.error === 'Invalid token' && error.error.message) {
            this.errorMessage = error.error.message;
          } else if (error.error.error === 'User not found. Please sign up first.') {
            this.errorMessage = error.error.error;
          } else if (error.error.error) {
            this.errorMessage = error.error.error;
          } else {
            this.errorMessage = 'Google login failed';
          }
        } else if (error.code) {
          // Handle Firebase authentication errors
          switch (error.code) {
            case 'auth/popup-closed-by-user':
              this.errorMessage = 'Login popup was closed. Please try again.';
              break;
            case 'auth/cancelled-popup-request':
              this.errorMessage = 'Login request was cancelled. Please try again.';
              break;
            case 'auth/popup-blocked':
              this.errorMessage = 'Login popup was blocked by your browser. Please allow popups for this site.';
              break;
            default:
              this.errorMessage = error.message || 'Google login failed';
          }
        } else {
          this.errorMessage = error.message || 'Google login failed';
        }
      });
  }
}
