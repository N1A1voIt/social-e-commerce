import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {routes} from "../../../app.routes";
import {VariantsService} from "./variants.service";
import {Variant} from "../products.types";
import {ApiResponse} from "../../inbox/inbox.service";
import {AsyncPipe, NgForOf, NgIf} from "@angular/common";
import {BasicButtonComponent} from "../../../shared/basic-button/basic-button.component";
import {BasicInputComponent} from "../../../shared/basic-input/basic-input.component";
import {BasicSelectComponent} from "../../../shared/basic-select/basic-select.component";
import {FormContainerComponent} from "../../../shared/form-container/form-container.component";
import {ProductListComponent} from "../product-list/product-list.component";
import {ReactiveFormsModule} from "@angular/forms";
import {VariantListComponent} from "./variant-list/variant-list.component";

@Component({
  selector: 'app-variants',
  standalone: true,
  imports: [
    AsyncPipe,
    BasicButtonComponent,
    BasicInputComponent,
    BasicSelectComponent,
    FormContainerComponent,
    NgForOf,
    NgIf,
    ProductListComponent,
    ReactiveFormsModule,
    VariantListComponent
  ],
  templateUrl: './variants.component.html',
  styleUrl: './variants.component.css'
})
export class VariantsComponent implements OnInit {
  variants:Variant[]=[];
  constructor(private router:ActivatedRoute,private variantService:VariantsService) {
  }
  ngOnInit(): void {
    this.fetchVariants();
  }
  fetchVariants() {
    const idProduct = <number> <unknown> this.router.snapshot.paramMap.get('idProduct');
    this.variantService.fetchVariants(idProduct).subscribe({
      next: (response:ApiResponse) => {
        this.variants = response.data;
      },error(err) {
        alert(err.errors[0].message);
      }
    })
  }

}
