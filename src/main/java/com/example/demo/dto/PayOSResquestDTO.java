package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSResquestDTO {
    private Long orderCode;
    private String description;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String buyerAddress;
    private List<ItemDTO> items;
    private String cancelUrl;
    private String returnUrl;

    @NonNull
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ItemDTO {
        private String name;
        private Integer quantity;
        private Integer price;
    }
}
