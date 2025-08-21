
export interface OrderParent {
  idOrderM?: number;
  description?: string;
  createdAt?: Date;
  dTotal?: number;
  dCustomerName?: string;
  dStatus?: number;
  shippingAddress?: string;
  customerNumber?: string;
  idPc?: string;
  childs?: OrderChild[];
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
}
