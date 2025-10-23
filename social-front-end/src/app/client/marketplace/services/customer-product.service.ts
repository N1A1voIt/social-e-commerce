import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { javaHost } from '../../../../environments/environment';

// Interfaces based on the API documentation
export interface ProductCPL {
  idPc: number;
  name: string;
  description: string;
  price: number;
  media: string;

  // other fields as needed
}

export interface ProductOptionDTO {
  idOption: number;
  label: string;
  optionValues: OptionValueDTO[];
}

export interface OptionValueDTO {
  idOv: number;
  value: string;
}

export interface SelectedOptionValues {
  productId: number;
  optionValueIds: number[];
}

export interface VariantInStock {
  idVariant: number;
  title: string;
  price: number;
  idProduct: number;
  createdAt: string;
  updatedAt: string;
  variantNumber: number;
  stockStatus: string;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerProductService {
  private baseUrl = `${javaHost}/api/customer/products`;

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Get all products
   */
  getProducts(page = 0, size = 10): Observable<ProductCPL[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<ProductCPL[]>(
      `${this.baseUrl}?page=${page}&size=${size}`,
      { headers }
    );
  }

  /**
   * Get product options
   */
  getProductOptions(productId: number): Observable<ProductOptionDTO[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<ProductOptionDTO[]>(
      `${this.baseUrl}/${productId}/options`,
      { headers }
    );
  }

  /**
   * Get variant by selected option values
   */
  getVariantByOptionValues(request: SelectedOptionValues): Observable<VariantInStock> {
    const headers = this.getAuthHeaders();
    return this.http.post<VariantInStock>(
      `${this.baseUrl}/variants`,
      request,
      { headers }
    );
  }
}
