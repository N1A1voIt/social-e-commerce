import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import {AccountDetailsComponent} from "./main/settings/account-details/account-details.component";
import {provideIcons} from "@ng-icons/core";
import {heroPencilSquare} from "@ng-icons/heroicons/outline";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, AccountDetailsComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: [provideIcons({heroPencilSquare})]
})
export class AppComponent {
  title = 'social-front-end';
}
