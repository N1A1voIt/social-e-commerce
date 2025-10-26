export interface CartItem {
    productId: number;
    productName: string;
    productMedia: string;
    variantId: number;
    variantTitle: string;
    price: number;
    quantity: number;
    totalPrice: number;
}

export interface Cart {
    cartId: number;
    idSeller: number;
    sellerName: string;
    customerId: number;
    createdAt: string; // Using string for LocalDateTime, can be converted to Date object if needed
    active: boolean;
    items: CartItem[];
    itemCount: number;
    totalPrice: number;
}
