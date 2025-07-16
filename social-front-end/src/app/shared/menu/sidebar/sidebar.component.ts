import { Component, OnDestroy } from '@angular/core';
import { TreeModule } from "@smart-webcomponents-angular/tree";
import { TreeExampleComponent } from "./tree-example/tree-example.component";
import { NgIcon } from "@ng-icons/core";
import { LinkComponent } from "./link/link.component";
import { NgIf } from "@angular/common";

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    TreeModule,
    TreeExampleComponent,
    NgIcon,
    LinkComponent,
    NgIf
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnDestroy {
  treeData = [
    {
      label: 'CEO',
      expanded: true,
      items: [
        {
          label: 'Manager 1',
          expanded: true,
          items: [
            { label: 'Employee 1' },
            { label: 'Employee 2' }
          ]
        },
        {
          label: 'Manager 2',
          expanded: true,
          items: [
            { label: 'Employee 3' }
          ]
        }
      ]
    }
  ];

  constructor() {}

  ngOnDestroy(): void {
    // Clean up resources if needed
  }
}
