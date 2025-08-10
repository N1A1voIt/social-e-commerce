import {Component, Input} from '@angular/core';
import {NgForOf} from "@angular/common";
import {Variant} from "../../products.types";

@Component({
  selector: 'app-variant-list',
  standalone: true,
    imports: [
        NgForOf
    ],
  templateUrl: './variant-list.component.html',
  styleUrl: './variant-list.component.css'
})
export class VariantListComponent {
  @Input() variantsList: Variant[] = [];
}
