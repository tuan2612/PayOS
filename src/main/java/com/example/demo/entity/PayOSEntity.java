package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PayOSEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    private String bin;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_name")
    private String accountName;

    private String buyerEmail;
    private String buyerName;
    private String buyerAddress;
    private String buyerPhone;

    private Integer amount;

    @Column(name = "order_code")
    private Long orderCode;

    private String reference;
    private String currency;

    @Column(name = "payment_link_id")
    private String paymentLinkId;

    private String status;

    @Column(name = "checkout_url")
    private String checkoutUrl;

    @Lob
    @Column(name = "qr_code")
    private String qrCode;

    private String signature;
    private String canceledAt;
    private String cancellationReason;
    private Long expireAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
    }

    @OneToMany(mappedBy = "payOSEntity")
    private List<ItemEntity> items;

    @OneToMany(mappedBy = "payOSEntity")
    private List<TransactionEntity> transaction;
}