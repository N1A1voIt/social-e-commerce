package com.itu.socialcom.demo.orders;

/**
 * Order Status Enum
 * Represents the possible states of an order
 */
public enum OrderStatus {
    PENDING(1, "Pending"),
    CONFIRMED(2, "Confirmed"),
    PROCESSING(3, "Processing"),
    SHIPPED(4, "Shipped"),
    DELIVERED(5, "Delivered"),
    CANCELLED(6, "Cancelled");

    private final int code;
    private final String label;

    OrderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING; // Default
    }
}

