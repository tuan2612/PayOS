package com.example.demo.service;

import com.example.demo.dto.PayOSResquestDTO;
import com.example.demo.entity.PayOSEntity;

import vn.payos.type.PaymentLinkData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

public interface PayOSService {
    PayOSEntity createPayment(PayOSResquestDTO form);

    PaymentLinkData getPaymentLink(Long order_id) throws Exception;

    PayOSEntity getPayOS(Long id);

    String successPayOs(Long orderCode);

    String confirmWebhook(String webhookUrl);

    PaymentLinkData cancelLink(Long orderCode, String mess);

    // Boolean isValidData(String transaction, String transactionSignature);
    boolean verifyPaymentWebhookData(Webhook webhookBody);
    void deletePayOSExprited(Long id);
}
