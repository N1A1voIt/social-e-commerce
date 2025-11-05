import { Component, OnDestroy } from '@angular/core';
import { TreeModule } from "@smart-webcomponents-angular/tree";
import { TreeExampleComponent } from "./tree-example/tree-example.component";
import { LinkComponent } from "./link/link.component";
import {Router} from "@angular/router";

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    TreeModule,
    TreeExampleComponent,
    LinkComponent
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnDestroy {
  activeSection: string = 'content'; // default to content section

  constructor(public router:Router) {}

  setActiveSection(section: string): void {
    this.activeSection = section;
  }

  ngOnDestroy(): void {
    // Clean up resources if needed
  }
}

