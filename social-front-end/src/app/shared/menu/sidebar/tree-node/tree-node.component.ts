import {Component, Input} from '@angular/core';
import {NgForOf, NgIf} from "@angular/common";
import {Router} from "@angular/router";
// tree-node.interface.ts
export interface TreeNode {
  key: string;
  label: string;
  data: string;
  icon: string;
  children?: TreeNode[];
  selectable?: boolean;
  link?:string;
}
@Component({
  selector: 'app-tree-node',
  standalone: true,
  imports: [
    NgForOf,
    NgIf
  ],
  templateUrl: './tree-node.component.html',
  styleUrl: './tree-node.component.css'
})
export class TreeNodeComponent {
  @Input() node!: TreeNode;
  @Input() depth: number = 0;
  @Input() isLast: boolean = false;
  isExpanded = true;
  constructor(public router:Router) {
  }
}
