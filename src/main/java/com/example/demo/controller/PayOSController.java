package com.example.demo.controller;

import com.example.demo.dto.PayOSResquestDTO;
import com.example.demo.dto.PaymentDataDTO;
import com.example.demo.dto.ResponseAPIDTO;
import com.example.demo.entity.PayOSEntity;
import com.example.demo.service.PayOSService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.PaymentLinkData;
import vn.payos.type.Webhook;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PayOSController {

    private final PayOSService payOSService;
    private final ModelMapper modelMapper;

    @GetMapping("/create")
    public ResponseEntity<ResponseAPIDTO<PaymentDataDTO>> createPayment(@RequestBody PayOSResquestDTO form) {
        // Dữ liệu mẫu
        PayOSResquestDTO sampleRequest = PayOSResquestDTO.builder()
                .description("Payment for Tuan")
                .buyerName("Nguyen Van A")
                .buyerEmail("buyer-email@example.com")
                .buyerPhone("090xxxxxxx")
                .buyerAddress("123, Main Street, District 1, Ho Chi Minh City")
                .cancelUrl("http://example.com/cancel")
                .returnUrl("http://example.com/success")
                .items(List.of(
                        PayOSResquestDTO.ItemDTO.builder()
                                .name("AirPods Pro")
                                .quantity(1)
                                .price(2000)
                                .build()))
                .build();

        // Kiểm tra dữ liệu đầu vào, sử dụng dữ liệu mẫu nếu cần
        if (form == null || form.getDescription() == null) {
            form = sampleRequest;
        }

        PayOSEntity response = payOSService.createPayment(form);
        PaymentDataDTO paymentDataDTO = modelMapper.map(response, PaymentDataDTO.class);

        ResponseAPIDTO<PaymentDataDTO> responseDTO = ResponseAPIDTO.<PaymentDataDTO>builder()
                .code("00")
                .data(paymentDataDTO)
                .desc("Success - Thành công")
                .signature(response.getSignature())
                .build();

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/success")
    public String success(@RequestParam Long orderCode) {
        return payOSService.successPayOs(orderCode);
    }

    @PostMapping("/cancel")
    public ResponseEntity<ResponseAPIDTO<PaymentLinkData>> cancel(@RequestParam Long orderCode,
            @RequestParam(required = false) String cancelMessage) {
        PaymentLinkData response = payOSService.cancelLink(orderCode, cancelMessage);
        PayOSEntity payOs = payOSService.getPayOS(orderCode);

        ResponseAPIDTO<PaymentLinkData> responseDTO = ResponseAPIDTO.<PaymentLinkData>builder()
                .code("00")
                .data(response)
                .desc("Success - Thành công")
                .signature(payOs.getSignature())
                .build();

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/getPaymentLinkInfo/{orderCode}")
    public ResponseEntity<ResponseAPIDTO<PaymentLinkData>> getPaymentLinkInformation(@PathVariable Long orderCode)
            throws Exception {
        PaymentLinkData response = payOSService.getPaymentLink(orderCode);
        PayOSEntity payOs = payOSService.getPayOS(orderCode);

        ResponseAPIDTO<PaymentLinkData> responseDTO = ResponseAPIDTO.<PaymentLinkData>builder()
                .code("00")
                .data(response)
                .desc("Success - Thành công")
                .signature(payOs.getSignature())
                .build();

        return ResponseEntity.ok(responseDTO);
    }

    // @PostMapping("/validate")
    // public Boolean validateTransaction(
    // @RequestBody formDTO form) {
    // return payOSService.isValidData(form.getData(), form.getSignature());
    // }

    @PostMapping("/webhook/verify")
    public String webhookVerify(@RequestBody Webhook webhookUrl) {
        boolean isVerified = payOSService.verifyPaymentWebhookData(webhookUrl);
        if (isVerified) {
            return "String thành công";
        } else {
            return "String không thành công";
        }
    }

    @PostMapping("/webhook/confirm")
    public String webhookConfirm(@RequestBody String entity) {
        return payOSService.confirmWebhook(entity);
    }

}