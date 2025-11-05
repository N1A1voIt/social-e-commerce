import {Component, Input, OnInit, OnChanges, SimpleChanges} from '@angular/core';
import {NodeService} from "./node.service";
import {TreeNode, TreeNodeComponent} from "../tree-node/tree-node.component";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-tree-example',
  templateUrl: './tree-example.component.html',
  styleUrl: './tree-example.component.css',
  imports: [
    NgForOf,
    TreeNodeComponent
  ],
  providers: [NodeService],
  standalone: true
})
export class TreeExampleComponent implements OnInit, OnChanges {
  @Input() treeData: TreeNode[] = [];
  @Input() activeSection: string = 'content';

  constructor(private nodeService: NodeService) {}

  ngOnInit() {
    this.loadTreeData();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['activeSection']) {
      this.loadTreeData();
    }
  }

  private loadTreeData() {
    switch(this.activeSection) {
      case 'content':
        this.treeData = this.nodeService.getContentNavigationData();
        break;
      case 'products':
        this.treeData = this.nodeService.getProductsNavigationData();
        break;
      case 'dashboard':
        this.treeData = this.nodeService.getDashboardNavigationData();
        break;
      default:
        this.treeData = this.nodeService.getContentNavigationData();
    }
  }
}

