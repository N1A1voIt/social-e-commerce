import {AfterViewInit, Component, OnDestroy, ViewChild} from '@angular/core';
import {TreeComponent, TreeModule} from "@smart-webcomponents-angular/tree";
import {TreeExampleComponent} from "./tree-example/tree-example.component";
import {NgIcon} from "@ng-icons/core";

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    TreeModule,
    TreeExampleComponent,
    NgIcon
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements AfterViewInit, OnDestroy {
  @ViewChild('tree', { read: TreeComponent, static: false }) tree!: TreeComponent;
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
  constructor() {
  }

  ngOnDestroy(): void {
        throw new Error('Method not implemented.');
    }

  ngAfterViewInit(): void {
    this.tree.showLines = true;
    this.tree.showRootLines = true;
  }

}
