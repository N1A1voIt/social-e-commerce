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

export interface TempLink {
  id: string;
  tempLink: string;
  expiredAt: string;
  phoneNumber: string;
  idOrderM: number;
  idSeller: number;
  amount: number;
  used: boolean;
}
