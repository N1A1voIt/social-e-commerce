import {Component, Input, OnInit} from '@angular/core';
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
export class TreeExampleComponent implements OnInit {
  @Input() treeData: TreeNode[] = [];

  constructor(private nodeService: NodeService) {}

  ngOnInit() {
    this.treeData = this.nodeService.getTreeNodesData();
  }
}
