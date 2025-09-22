export interface TransactionStatus {
  status?:string;
  transactionId?:string;
  correlationId?:string;
  rawResponse?:string;
}
export interface TransactionDetail {
  amount?:number;
  description?:string;
  phoneNumber?:string;
  provider?:string;
  idPayment?:string;
}
