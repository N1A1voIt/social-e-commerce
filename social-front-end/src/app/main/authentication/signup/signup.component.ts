import { Component } from '@angular/core';
import {LoginInputComponent} from "../../../shared/forms/auth/login-input/login-input.component";
import {PlatformButtonComponent} from "../../../shared/forms/auth/platform-button/platform-button.component";
import { Auth, createUserWithEmailAndPassword, signInWithPopup, GoogleAuthProvider, updateProfile } from '@angular/fire/auth';
import { HttpClient } from '@angular/common/http';

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

  constructor(private auth: Auth, private http: HttpClient) {}

  name = '';
  username = '';
  email = '';
  password = '';


  signup() {
    createUserWithEmailAndPassword(this.auth, this.email, this.password)
      .then(userCredential => {
        return updateProfile(userCredential.user, { displayName: this.name }).then(() => userCredential);
      })
      .then(userCredential => userCredential.user.getIdToken())
      .then(idToken => {
        this.http.post('/api/auth/signup', {
          idToken,
          name: this.name,
          username: this.username
        }).subscribe(
          res => {  },
          err => { /* handle error */ }
        );
      })
      .catch(error => {
        alert(error.message);
      });
  }
  
  signupWithGoogle() {
    signInWithPopup(this.auth, new GoogleAuthProvider())
      .then(userCredential => userCredential.user.getIdToken())
      .then(idToken => {
        return this.http.post('/api/auth/signup', { idToken }).subscribe();
      });
  }
}
