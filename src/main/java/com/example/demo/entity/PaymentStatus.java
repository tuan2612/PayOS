package com.example.demo.entity;

public enum PaymentStatus {

    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED;

    public static PaymentStatus toEnum(String role) {
        for (PaymentStatus item : PaymentStatus.values()) {
            if (item.toString().equals(role))
                return item;
        }
        return null;
    }
}
