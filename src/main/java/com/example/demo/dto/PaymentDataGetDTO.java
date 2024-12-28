package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDataGetDTO {
    private String id;
    private Long orderCode;
    private Double amount;
    private Double amountPaid;
    private Double amountRemaining;
    private String status;
    private String createdAt;
    private List<TransactionDTO> transactions;
    private String cancellationReason;
    private String canceledAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDTO {
        private String reference;
        private Double amount;
        private String accountNumber;
        private String description;
        private LocalDateTime transactionDateTime;
        private String virtualAccountName;
        private String virtualAccountNumber;
        private String counterAccountBankId;
        private String counterAccountBankName;
        private String counterAccountName;
        private String counterAccountNumber;
    }
}
