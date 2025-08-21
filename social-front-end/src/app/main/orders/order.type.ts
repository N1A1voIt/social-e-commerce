
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
