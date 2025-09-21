export interface Delivery {
    id: number;
    shippingAddress: string;
    endedAt?: Date;
    phoneNumber: string;
    startedAt: Date;
    status: string;
    amount?: number;
    shippingPointId: number;
    orderMotherId: number;
    deliveryDriverId?: number;
    distance?: number;
}

export interface DeliveryResponse {
    data: Delivery[];
    message: string;
    status: string;
}
