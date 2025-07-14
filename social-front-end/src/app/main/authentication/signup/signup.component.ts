import {Component, OnInit} from '@angular/core';
import {LoginInputComponent} from "../../../shared/forms/auth/login-input/login-input.component";
import {PlatformButtonComponent} from "../../../shared/forms/auth/platform-button/platform-button.component";
import { Auth, createUserWithEmailAndPassword, signInWithPopup, GoogleAuthProvider, updateProfile } from '@angular/fire/auth';
import { HttpClient } from '@angular/common/http';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {javaHost} from "../../../../environments/environment";

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [
    LoginInputComponent,
    PlatformButtonComponent,
    ReactiveFormsModule
  ],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent implements OnInit{
  signupForm!: FormGroup;

  constructor(private fb: FormBuilder, private auth: Auth, private http: HttpClient) {}

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
        alert('Signup successful! Token: ' + response);
      })
      .catch(error => {
        const message = error.error?.error || error.message || 'Signup failed';
        alert(`Error: ${message}`);
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
        alert('Google signup successful! Token: ' + response);
      })
      .catch(error => {
        const message = error.error?.error || error.message || 'Google signup failed';
        alert(`Error: ${message}`);
      });
  }

}
