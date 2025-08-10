import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {
  Variant,
  VariantWithOptionsDTO,
  CreateVariantWithOptionsRequest,
  GenerateVariantsRequest,
  UpdateVariantRequest
} from "../products.types";
import {javaHost} from "../../../../environments/environment";
import {Observable} from "rxjs";
import {ApiResponse} from "../../inbox/inbox.service";

@Injectable({
  providedIn: 'root'
})
export class VariantsService {

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token')?.replace('Bearer ', '');
    return new HttpHeaders({
      'Authorization': `${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Fetch variants with their option details for a product
   */
  fetchVariantsWithOptions(productId: number): Observable<ApiResponse> {
    const headers = this.getAuthHeaders();
    return this.http.get<ApiResponse>(
      `${javaHost}/api/variants/products/${productId}/with-options`,
      { headers }
    );
  }

  /**
   * Create a single variant with specific option values
   */
  createVariantWithOptions(productId: number, request: CreateVariantWithOptionsRequest): Observable<VariantWithOptionsDTO> {
    const headers = this.getAuthHeaders();
    return this.http.post<VariantWithOptionsDTO>(
      `${javaHost}/api/variants/products/${productId}/with-options`,
      request,
      { headers }
    );
  }

  /**
   * Generate all possible variant combinations for a product
   */
  generateAllVariants(productId: number, request: GenerateVariantsRequest): Observable<VariantWithOptionsDTO[]> {
    const headers = this.getAuthHeaders();
    return this.http.post<VariantWithOptionsDTO[]>(
      `${javaHost}/api/variants/products/${productId}/generate-all`,
      request,
      { headers }
    );
  }

  /**
   * Update an existing variant
   */
  updateVariant(productId: number, variantId: number, request: UpdateVariantRequest): Observable<VariantWithOptionsDTO> {
    const headers = this.getAuthHeaders();
    return this.http.put<VariantWithOptionsDTO>(
      `${javaHost}/api/variants/products/${productId}/variants/${variantId}`,
      request,
      { headers }
    );
  }

  /**
   * Delete a variant
   */
  deleteVariant(productId: number, variantId: number): Observable<{message: string}> {
    const headers = this.getAuthHeaders();
    return this.http.delete<{message: string}>(
      `${javaHost}/api/variants/products/${productId}/variants/${variantId}`,
      { headers }
    );
  }

  /**
   * Legacy method - kept for backward compatibility
   */
  fetchVariants(idProduct: number): Observable<ApiResponse> {
    return this.http.get<ApiResponse>(javaHost + '/api/variants/' + idProduct);
  }
}
