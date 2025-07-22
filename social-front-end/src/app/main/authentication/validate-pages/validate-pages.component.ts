import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {PageService} from "./page.service";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-validate-pages',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './validate-pages.component.html',
  styleUrl: './validate-pages.component.css'
})
export class ValidatePagesComponent implements OnInit{
  platform: string = '';
  uuid: string = '';
  valid:boolean = false;
  constructor(private route: ActivatedRoute,private pageService: PageService) {}
  errorMessage: string = '';
  ngOnInit(): void {
    this.platform = this.route.snapshot.paramMap.get('platform') || '';
    this.uuid = this.route.snapshot.queryParamMap.get('uuid') || '';
  }
  validate(): void {
    const token = localStorage.getItem('token')?.replace('Bearer ', '') || '';
    this.pageService.validate(this.platform, this.uuid,token).subscribe({
      next: (pages) => {
        console.log('Managed pages received:',pages);
        this.valid = true;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred during validation.';
        console.error('Validation failed:', err);
      },
    });
  }
}
