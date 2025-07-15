export interface TokenDTO {
  token: string;
}

export interface TokenValidationResponse {
  status: number;
  data: {
    valid: boolean;
  };
}
