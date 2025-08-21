export interface TempProduct {
  idProduct?: number;
  name: string;
  description: string;
  price: number;
  media: string;
  idSeller: number;
  idCategory: number;
  state?: boolean;
}

export interface OptionValueDTO {
  optionLabels: string;
  values: string[];
}
export interface Category {
  idCategory: number;
  val: string;
  description: string;
}
export interface CreationStepsDTO {
  sessionId: string;
  step1: TempProduct;
  step2: OptionValueDTO[];
}

export interface DisplayProduct {
  creationStepsDTO : CreationStepsDTO,
  categories : Category[]
}

export interface Product {
  idProduct: number,
  name: string,
  description: string,
  price: number,
  media: string,
  idSeller: number,
  createdAt: Date,
  updatedAt: Date,
  idCategory: number,
  formattedPrice: number
}

export interface ProductCpl {
  idPc: number,
  description: string,
  name: string,
  price: number,
  media: string,
  idSeller: number,
  idCategory: number,
  categoryName: string,
  productNumber: number
  stockStatus: string
}

export interface Variant {
  idVariant:number;
  title:string;
  price:number;
  idProduct:number;
  createdAt:Date;
  updatedAt:Date;
  mediaUrl:string;
}

export interface VariantOptionDTO {
  idOption: number;
  optionLabel: string;
  idOptionValue: number;
  optionValue: string;
}

export interface VariantWithOptionsDTO {
  idVariant: number;
  title: string;
  price: number;
  idProduct: number;
  createdAt: Date;
  updatedAt: Date;
  stockQuantity?: number;
  stockStatus?: string;
  options: VariantOptionDTO[];
}

export interface CreateVariantWithOptionsRequest {
  sku:string;
  media:string;
  title: string;
  price: number;
  optionValueIds: number[];
}

export interface GenerateVariantsRequest {
  basePrice: number;
  titlePrefix?: string;
  overwriteExisting?: boolean;
}

export interface UpdateVariantRequest {
  title?: string;
  price?: number;
}

export interface ProductOption {
  idOption: number;
  label: string;
  idProduct: number;
  optionValues: ProductOptionValue[];
}

export interface ProductOptionValue {
  idOv: number;
  value: string;
  idOption: number;
}

export interface OrderPreview {
  variants: VariantWithQuantity[];
  customerName: string;
  customerNumber: string;
  shippingAddress: string;
  idPc?:string
}

export interface MessageOrdering {
  variants: VariantWithQuantity[];
  customerName: string;
  customerNumber: string;
  shippingAddress: string;
}

export interface VariantWithQuantity {
  variant: Variant;
  quantity: number;
}
