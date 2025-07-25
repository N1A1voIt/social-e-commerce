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

export interface CreationStepsDTO {
  sessionId: string;
  step1: TempProduct;
  step2: OptionValueDTO[];
}
