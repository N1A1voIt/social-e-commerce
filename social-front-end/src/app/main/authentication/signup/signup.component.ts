import {Component, OnInit} from '@angular/core';
import {LoginInputComponent} from "../../../shared/forms/auth/login-input/login-input.component";
import {PlatformButtonComponent} from "../../../shared/forms/auth/platform-button/platform-button.component";
import { Auth, createUserWithEmailAndPassword, signInWithPopup, GoogleAuthProvider, updateProfile } from '@angular/fire/auth';
import { HttpClient } from '@angular/common/http';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {javaHost} from "../../../../environments/environment";
import {Router} from "@angular/router";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [
    LoginInputComponent,
    PlatformButtonComponent,
    ReactiveFormsModule,
    NgIf
  ],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent implements OnInit{
  signupForm!: FormGroup;
  errorMessage: string = '';

  constructor(private fb: FormBuilder, private auth: Auth, private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.signupForm = this.fb.group({
      name: ['', Validators.required],
      username: [''],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  signup() {
    const { email, password, name, username } = this.signupForm.value;

    createUserWithEmailAndPassword(this.auth, email, password)
      .then(userCredential =>
        updateProfile(userCredential.user, { displayName: name }).then(() => userCredential)
      )
      .then(userCredential => userCredential.user.getIdToken())
      .then(idToken => this.http.post(`${javaHost}/api/auth/signup`, { idToken, name, username }).toPromise())
      .then((response: any) => {
        // Store token in localStorage
        localStorage.setItem('token',"Bearer " + response.token);
        this.errorMessage = ''; // Clear any previous error
        this.router.navigate(['/basic/dashboard']);
      })
      .catch(error => {
        if (error.error) {
          // Handle backend error response
          if (error.error.error === 'Invalid token' && error.error.message) {
            this.errorMessage = error.error.message;
          } else if (error.error.error) {
            this.errorMessage = error.error.error;
          } else {
            this.errorMessage = 'Signup failed';
          }
        } else if (error.code) {
          // Handle Firebase authentication errors
          switch (error.code) {
            case 'auth/email-already-in-use':
              this.errorMessage = 'Email is already in use. Please use a different email or try logging in.';
              break;
            case 'auth/invalid-email':
              this.errorMessage = 'Invalid email format. Please enter a valid email address.';
              break;
            case 'auth/weak-password':
              this.errorMessage = 'Password is too weak. Please use a stronger password.';
              break;
            case 'auth/operation-not-allowed':
              this.errorMessage = 'Email/password accounts are not enabled. Please contact support.';
              break;
            default:
              this.errorMessage = error.message || 'Signup failed';
          }
        } else {
          this.errorMessage = error.message || 'Signup failed';
        }
      });
  }

  signupWithGoogle() {
    signInWithPopup(this.auth, new GoogleAuthProvider())
      .then(userCredential => userCredential.user.getIdToken())
      .then(idToken => {
        const { name, username } = this.signupForm.value;
        return this.http.post(`${javaHost}/api/auth/signup`, { idToken, name, username }).toPromise();
      })
      .then((response: any) => {
        // Store token in localStorage
        localStorage.setItem('token',"Bearer " +  response.token);
        this.errorMessage = ''; // Clear any previous error
        this.router.navigate(['/basic/dashboard']);
      })
      .catch(error => {
        if (error.error) {
          // Handle backend error response
          if (error.error.error === 'Invalid token' && error.error.message) {
            this.errorMessage = error.error.message;
          } else if (error.error.error) {
            this.errorMessage = error.error.error;
          } else {
            this.errorMessage = 'Google signup failed';
          }
        } else if (error.code) {
          // Handle Firebase authentication errors
          switch (error.code) {
            case 'auth/popup-closed-by-user':
              this.errorMessage = 'Signup popup was closed. Please try again.';
              break;
            case 'auth/cancelled-popup-request':
              this.errorMessage = 'Signup request was cancelled. Please try again.';
              break;
            case 'auth/popup-blocked':
              this.errorMessage = 'Signup popup was blocked by your browser. Please allow popups for this site.';
              break;
            case 'auth/account-exists-with-different-credential':
              this.errorMessage = 'An account already exists with the same email address but different sign-in credentials. Please sign in using the original method.';
              break;
            default:
              this.errorMessage = error.message || 'Google signup failed';
          }
        } else {
          this.errorMessage = error.message || 'Google signup failed';
        }
      });
  }

}
