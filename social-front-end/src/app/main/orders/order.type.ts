
export interface OrderParent {
  idOrderM?: number;
  description?: string;
  createdAt?: Date;
  dtotal?: number;
  dcustomerName?: string;
  dstatus?: number;
  shippingAddress?: string;
  customerNumber?: string;
  idPc?: string;
  idManagedPages?:number;
  childs?: OrderChild[];
}
export interface OrderDisplay {
  orders : OrderParent[];
  totalOrders : number;
}

export interface OrderChild {
  idOrderDetails?: number;
  price?: number;
  quantity?: number;
  idVariant?: number;
  idProduct?: number;
  idOrderM?: number;
  mediaUrl?: string;
  productName?:string;
  sku?: string;
  idManagedPages?: number;
}
export interface shippingPoints {
  idShippingPoint?: number;
  name?: string;
  address?: string;

}


export interface DeliveryMission {
  orderParent:OrderParent;
  shippingPointId?:number;
}
export interface DeliveryApplicant {
  id: number;                // Long -> number
  shippingAddress?: string;   // String -> string, optional if nullable
  idDelivery?: number;
  idShp?: number;
  dStatus?: string;
  amount?: number;
  distance?: number;
  idDd?: number;
  driverName?: string;
  driverPhone?: string;
  idMp?: number;
  pageTitle?: string;
  idSeller?: number;          // Integer -> number
  email?: string;
  username?: string;
  firebaseUid?: string;
}

export interface RefundRequest {
  orderId: number;
  amount: number;
}

export interface Refund {
  id?: number;
  orderId: number;
  amount: number;
  saleId?: number;
  createdAt?: Date;
}

export interface OrderPayment {
  id?: number;
  salesId?: number;
  amount?: number;
  paymentMethodId?: number;
  paymentMethodName?: string;
  createdAt?: Date;
}
