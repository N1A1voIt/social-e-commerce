import { Component, OnInit } from '@angular/core';
import {LoginInputComponent} from "../../../shared/forms/auth/login-input/login-input.component";
import {PlatformButtonComponent} from "../../../shared/forms/auth/platform-button/platform-button.component";
import { Auth, signInWithEmailAndPassword, signInWithPopup, GoogleAuthProvider } from '@angular/fire/auth';
import { HttpClient } from '@angular/common/http';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {javaHost} from "../../../../environments/environment";
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    LoginInputComponent,
    PlatformButtonComponent,
    ReactiveFormsModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;

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
      .then(idToken => this.http.post(`${javaHost}/api/auth/signin`, { idToken }).toPromise())
      .then((response: any) => {
        // Store token in localStorage
        localStorage.setItem('token', response.token);
        alert('Login successful!');
        this.router.navigate(['/basic/dashboard']);
      })
      .catch(error => {
        const message = error.error?.error || error.message || 'Login failed';
        alert(`Error: ${message}`);
      });
  }

  loginWithGoogle() {
    signInWithPopup(this.auth, new GoogleAuthProvider())
      .then(userCredential => userCredential.user.getIdToken())
      .then(idToken => this.http.post(`${javaHost}/api/auth/signin`, { idToken }).toPromise())
      .then((response: any) => {
        // Store token in localStorage
        localStorage.setItem('token', response.token);
        alert('Google login successful!');
        this.router.navigate(['/basic/dashboard']);
      })
      .catch(error => {
        const message = error.error?.error || error.message || 'Google login failed';
        alert(`Error: ${message}`);
      });
  }
}
