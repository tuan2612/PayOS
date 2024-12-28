package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseAPIDTO<T> {
    @Builder.Default
    private String code = "00";
    private String desc;
    private T data;
    private String signature;
}
