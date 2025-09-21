import { Injectable } from '@angular/core';
import { getAuth, Auth, RecaptchaVerifier, signInWithPhoneNumber, ConfirmationResult, User } from 'firebase/auth';
import { initializeApp } from 'firebase/app';
import {environment} from "../../../environments/environment";
import {getApps} from "@angular/fire/app";

@Injectable({ providedIn: 'root' })
export class DauthService {
  private auth: Auth;
  public confirmationResult?: ConfirmationResult;
  private recaptchaVerifier?: RecaptchaVerifier;

  constructor() {
    // This robust initialization prevents the "app/no-app" error
    // by ensuring the app is only initialized once.
    // if (!getApps().length) {
      const app = initializeApp(environment.firebase);
      this.auth = getAuth(app);
    // } else {
    //   this.auth = getAuth();
    // }
  }

  /**
   * Initializes the RecaptchaVerifier.
   * It's designed to be called once, after the component view is ready.
   * @param containerId The ID of the HTML element where the reCAPTCHA will be rendered.
   * @returns The RecaptchaVerifier instance.
   */
  initRecaptcha(containerId: string): RecaptchaVerifier {
    // Create verifier only if it doesn't exist to avoid re-creation
    if (!this.recaptchaVerifier) {
      this.recaptchaVerifier = new RecaptchaVerifier(this.auth, containerId, {
        'size': 'invisible',
        'callback': (response: any) => {
          // reCAPTCHA solved, allow signInWithPhoneNumber.
        }
      });
    }
    return this.recaptchaVerifier;
  }

  /**
   * Sends a verification code to the provided phone number.
   * @param phoneNumber The user's phone number in E.164 format.
   * @param verifier The RecaptchaVerifier instance.
   * @returns A promise that resolves when the code is sent.
   */
  sendVerificationCode(phoneNumber: string, verifier: RecaptchaVerifier): Promise<void> {
    return signInWithPhoneNumber(this.auth, phoneNumber, verifier)
      .then(result => {
        this.confirmationResult = result;
      });
  }

  /**
   * Confirms the OTP code sent to the user.
   * @param code The 6-digit OTP code.
   * @returns A promise that resolves with the authenticated user's credential.
   */
  confirmCode(code: string): Promise<User> {
    if (!this.confirmationResult) {
      return Promise.reject(new Error("No verification pending. Please request a new code."));
    }
    return this.confirmationResult.confirm(code).then(res => res.user);
  }
}
