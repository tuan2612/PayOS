package com.example.demo.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction_entity")
public class TransactionEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "transaction_date_time", nullable = false)
    private String transactionDateTime;

    @Column(name = "virtual_account_name")
    private String virtualAccountName;

    @Column(name = "virtual_account_number")
    private String virtualAccountNumber;

    @Column(name = "counter_account_bank_id")
    private String counterAccountBankId;

    @Column(name = "counter_account_bank_name")
    private String counterAccountBankName;

    @Column(name = "counter_account_name")
    private String counterAccountName;

    @Column(name = "counter_account_number")
    private String counterAccountNumber;
    @ManyToOne
    @JoinColumn(name = "payos_id")
    private PayOSEntity payOSEntity;
}
