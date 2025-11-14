import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { NgIcon } from '@ng-icons/core';
import { javaHost } from '../../../../environments/environment';

interface DeliveryProfile {
  id: number;
  name: string;
  email: string;
  phoneNumber: string;
  minRange: number;
  maxRange: number;
}

@Component({
  selector: 'app-dprofile',
  standalone: true,
  imports: [CommonModule, NgIcon, FormsModule],
  templateUrl: './dprofile.component.html',
  styleUrl: './dprofile.component.css'
})
export class DprofileComponent implements OnInit {
  profile: DeliveryProfile | null = null;
  loading: boolean = true;
  error: string | null = null;
  isEditing: boolean = false;
  saving: boolean = false;
  successMessage: string | null = null;

  // Edit form values
  editForm = {
    phoneNumber: '',
    minRange: 0,
    maxRange: 0
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    const token = localStorage.getItem('token');
    if (!token) {
      this.error = 'No authentication token found';
      this.loading = false;
      return;
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    this.http.get<any>(`${javaHost}/api/delivery/profile/me`, { headers })
      .subscribe({
        next: (response) => {
          if (response.status === 200) {
            this.profile = response.data;
            this.resetEditForm();
          } else {
            this.error = 'Failed to load profile';
          }
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load profile data';
          this.loading = false;
          console.error('Profile error:', err);
        }
      });
  }

  resetEditForm(): void {
    if (this.profile) {
      this.editForm = {
        phoneNumber: this.profile.phoneNumber,
        minRange: this.profile.minRange,
        maxRange: this.profile.maxRange
      };
    }
  }

  startEdit(): void {
    this.isEditing = true;
    this.resetEditForm();
    this.successMessage = null;
    this.error = null;
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.resetEditForm();
    this.error = null;
  }

  saveChanges(): void {
    if (!this.validateForm()) {
      this.error = 'Please ensure max range is greater than min range';
      return;
    }

    this.saving = true;
    this.error = null;
    this.successMessage = null;

    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    const updates = {
      phoneNumber: this.editForm.phoneNumber,
      minRange: this.editForm.minRange,
      maxRange: this.editForm.maxRange
    };

    this.http.put<any>(`${javaHost}/api/delivery/profile/update`, updates, { headers })
      .subscribe({
        next: (response) => {
          if (response.status === 200) {
            this.profile = response.data;
            this.isEditing = false;
            this.successMessage = 'Profile updated successfully!';
            setTimeout(() => {
              this.successMessage = null;
            }, 3000);
          } else {
            this.error = 'Failed to update profile';
          }
          this.saving = false;
        },
        error: (err) => {
          this.error = 'Failed to update profile. Please try again.';
          this.saving = false;
          console.error('Update error:', err);
        }
      });
  }

  validateForm(): boolean {
    return this.editForm.minRange < this.editForm.maxRange;
  }
}
