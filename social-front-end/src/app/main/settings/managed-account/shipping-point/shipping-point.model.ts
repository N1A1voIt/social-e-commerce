export interface ShippingPoint {
    id?: number;
    placeName: string;
    latitude?: number;
    longitude?: number;
    distance: number;
    origin?: string;
    managedPageId: number;
}
