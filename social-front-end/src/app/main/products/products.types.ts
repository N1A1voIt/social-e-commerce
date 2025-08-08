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
  idProduct: number,
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
