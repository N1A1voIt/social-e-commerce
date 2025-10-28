import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import type {SelectOption} from "../../../../shared/basic-select/basic-select.component";
import {javaHost} from "../../../../../environments/environment";

// Types for Prompt Saver API
export type PlatformLabel = 'facebook' | 'instagram' | 'x' | 'thread';

export interface PlatformInfo {
  id: number;
  label: PlatformLabel;
}

export interface PromptSaverResponse {
  id: number;
  prompt: string;
  sellerId: number;
  platformId: number;
  platformLabel: PlatformLabel;
  createdAt: string; // ISO datetime
}

export interface PromptSaverRequest {
  prompt: string;
  platformId: number;
}

@Component({
  selector: 'app-prompt-parameter',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './prompt-parameter.component.html',
  styleUrl: './prompt-parameter.component.css'
})
export class PromptParameterComponent implements OnInit {
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);

  form!: FormGroup;
  loading = false;
  saving = false;
  error: string | null = null;

  platforms: PlatformInfo[] = [];
  platformOptions: SelectOption[] = [];
  prompts: PromptSaverResponse[] = [];

  readonly baseUrl = javaHost+'/api/prompts';

  ngOnInit(): void {
    this.form = this.fb.group({
      platformId: [null, Validators.required],
      prompt: ['', [Validators.required, Validators.minLength(10)]]
    });

    // initial loads
    this.loadPlatforms();
    this.loadPrompts();
  }

  private get authHeaders(): HttpHeaders {
    const raw = localStorage.getItem('token') || '';
    const token = raw.startsWith('Bearer ') ? raw : `Bearer ${raw}`;
    return new HttpHeaders({
      Authorization: token.replace("Bearer "," "),
      'Content-Type': 'application/json'
    });
  }

  async loadPlatforms(): Promise<void> {
    this.loading = true;
    this.error = null;
    try {
      const res = await this.http.get<PlatformInfo[]>(`${this.baseUrl}/platforms`, { headers: this.authHeaders }).toPromise();
      this.platforms = res || [];
      this.platformOptions = this.platforms.map(p => ({ value: p.id, label: p.label }));
    } catch (e: any) {
      this.error = 'Failed to load platforms';
      console.error(e);
    } finally {
      this.loading = false;
    }
  }

  async loadPrompts(): Promise<void> {
    this.loading = true;
    this.error = null;
    try {
      const res = await this.http.get<PromptSaverResponse[]>(`${this.baseUrl}`, { headers: this.authHeaders }).toPromise();
      this.prompts = (res || []).sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    } catch (e: any) {
      this.error = 'Failed to load prompts';
      console.error(e);
    } finally {
      this.loading = false;
    }
  }

  async savePrompt(): Promise<void> {
    if (this.form.invalid) return;

    this.saving = true;
    this.error = null;
    const body: PromptSaverRequest = {
      prompt: this.form.value.prompt,
      platformId: Number(this.form.value.platformId)
    };

    try {
      await this.http.post<PromptSaverResponse>(`${this.baseUrl}`, body, { headers: this.authHeaders }).toPromise();
      // reload list and reset form
      await this.loadPrompts();
      this.form.reset();
    } catch (e: any) {
      this.error = 'Failed to save prompt';
      console.error(e);
    } finally {
      this.saving = false;
    }
  }

  async deletePrompt(platformId: number): Promise<void> {
    if (!platformId) return;
    this.loading = true;
    this.error = null;
    try {
      await this.http.delete(`${this.baseUrl}/platform/${platformId}`, { headers: this.authHeaders }).toPromise();
      await this.loadPrompts();
    } catch (e: any) {
      this.error = 'Failed to delete prompt';
      console.error(e);
    } finally {
      this.loading = false;
    }
  }

  platformLabel(id: number): string {
    return this.platforms.find(p => p.id === id)?.label || String(id);
  }

  formatDate(date: string | Date): string {
    const d = typeof date === 'string' ? new Date(date) : date;
    return isNaN(d.getTime()) ? '' : d.toLocaleDateString();
  }
}
