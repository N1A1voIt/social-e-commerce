import {Component, Input} from '@angular/core';
import {FormContainerComponent} from "../../../shared/form-container/form-container.component";
import {BasicInputComponent} from "../../../shared/basic-input/basic-input.component";
import {NgIf} from "@angular/common";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {BeautifulButtonComponent} from "../../../shared/beautiful-button/beautiful-button.component";
import {TransactionDetail} from "../transaction.type";
import {ActivatedRoute, Router} from "@angular/router";
import {routes} from "../../../app.routes";
import {TransactionService} from "../transaction.service";
import {ApiResponse} from "../../inbox/inbox.service";
import {MessageService} from "primeng/api";

@Component({
  selector: 'app-mvola',
  standalone: true,
  providers:[MessageService],
  imports: [
    FormContainerComponent,
    BasicInputComponent,
    NgIf,
    ReactiveFormsModule,
    BeautifulButtonComponent,
    FormsModule
  ],
  templateUrl: './mvola.component.html',
  styleUrl: './mvola.component.css'
})
export class MvolaComponent {
  @Input() isVisible:boolean = false;
  mvolaForm!:FormGroup;
  constructor(formBuilder:FormBuilder,private messageService:MessageService,private naavigateRouter:Router,private router:ActivatedRoute,private  transactionService:TransactionService) {
    this.mvolaForm = formBuilder.group({
      'amount' : [0,Validators.min(100)],
      'description' : ['',Validators.required],
      'phoneNumber' : ['',[Validators.pattern(/^(\0\d{9}|\d{9})$/),Validators.required]]
    })
  }
  executeTransaction() {
    let transactionBody:TransactionDetail = this.mvolaForm.value;
    transactionBody.provider = 'mvola';
    console.log(this.router.snapshot.queryParamMap?.get("id_payment"))
    transactionBody.idPayment = this.router.snapshot.queryParamMap?.get("id_payment") || "";
    this.transactionService.mobilePay(transactionBody).subscribe({
      next: (data: ApiResponse) => {
        this.isVisible = false;
        this.naavigateRouter.navigateByUrl("/success-redirection");
        this.messageService.add({ severity: 'info', summary: 'Deliverer assigned', detail: `Transaction Succesfull`, life: 3000 })
      },error : (err:any) => {
        // alert(err.errors)
        this.messageService.add({severity: 'error', summary: 'Error', detail: 'Transaction failed'});
        alert(err.error.errors[0].message)
      }
    })
  }
}
