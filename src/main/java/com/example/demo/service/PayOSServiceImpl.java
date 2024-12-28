package com.example.demo.service;

import com.example.demo.dto.PayOSResquestDTO;
import com.example.demo.entity.PayOSEntity;
import com.example.demo.repository.PayOSRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PayOSServiceImpl implements PayOSService {

    private final PayOSRepository payOSRepository;
    private static final Logger logger = LoggerFactory.getLogger(PayOSServiceImpl.class);
    private final PayOS payOS;
    private final String domain = "http://localhost:3200/success";
    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Override
    public PayOSEntity createPayment(PayOSResquestDTO form) {
        Long orderCode = System.currentTimeMillis() / 1000;

        List<ItemData> itemsData = form.getItems().stream()
                .map(item -> ItemData.builder()
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        Integer totalPrice = (int) form.getItems().stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
        Long currentTimeMillis = System.currentTimeMillis();
        Long expiredAt = (currentTimeMillis / 1000) + 3 * 60;
        PaymentData paymentData = PaymentData.builder()
                .expiredAt(expiredAt)
                .orderCode(orderCode)
                .amount(totalPrice)
                .buyerName(form.getBuyerName())
                .buyerEmail(form.getBuyerEmail())
                .buyerAddress(form.getBuyerAddress())
                .buyerPhone(form.getBuyerPhone())
                .description(form.getDescription())
                .returnUrl(domain + "success")
                .cancelUrl(domain + "cancel ")
                .items(itemsData)
                .build();

        try {
            String signature = generateSignature(paymentData, checksumKey);
            CheckoutResponseData payCheck = payOS.createPaymentLink(paymentData);
            PayOSEntity payOSEntity = PayOSEntity.builder()
                    .expireAt(paymentData.getExpiredAt())
                    .orderCode(orderCode)
                    .amount(paymentData.getAmount())
                    .signature(signature)
                    .description(paymentData.getDescription())
                    .accountName(payCheck.getAccountName())
                    .accountNumber(payCheck.getAccountNumber())
                    .buyerAddress(paymentData.getBuyerAddress())
                    .buyerEmail(paymentData.getBuyerEmail())
                    .buyerPhone(paymentData.getBuyerPhone())
                    .buyerName(paymentData.getBuyerName())
                    .currency(payCheck.getCurrency())
                    .paymentLinkId(payCheck.getPaymentLinkId())
                    .status(payCheck.getStatus())
                    .checkoutUrl(payCheck.getCheckoutUrl())
                    .qrCode(payCheck.getQrCode())
                    .bin(payCheck.getBin())
                    .build();
            payOSRepository.save(payOSEntity);
            return payOSEntity;
        } catch (Exception e) {
            log.error("Error while creating payment: ", e);
        }
        return null;
    }

    public static String generateSignature(PaymentData paymentData, String checksumKey) throws Exception {
        Map<String, String> params = new TreeMap<>();
        params.put("amount", String.valueOf(paymentData.getAmount()));
        params.put("cancelUrl", paymentData.getCancelUrl());
        params.put("description", paymentData.getDescription());
        params.put("orderCode", String.valueOf(paymentData.getOrderCode()));
        params.put("returnUrl", paymentData.getReturnUrl());

        Iterator<Map.Entry<String, String>> sortedIterator = sortedIterator(params.entrySet().iterator(),
                Map.Entry.comparingByKey());

        StringBuilder transactionStr = new StringBuilder();
        while (sortedIterator.hasNext()) {
            Map.Entry<String, String> entry = sortedIterator.next();
            transactionStr.append(entry.getKey()).append("=").append(entry.getValue());
            if (sortedIterator.hasNext()) {
                transactionStr.append("&");
            }
        }

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hash = sha256_HMAC.doFinal(transactionStr.toString().getBytes(StandardCharsets.UTF_8));

        StringBuilder signature = new StringBuilder();
        for (byte b : hash) {
            signature.append(String.format("%02x", b));
        }

        return signature.toString();
    }

    public static <T> Iterator<T> sortedIterator(Iterator<T> it, Comparator<T> comparator) {
        List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        list.sort(comparator);
        return list.iterator();
    }

    @Override
    public PaymentLinkData getPaymentLink(Long order_id) {
        PaymentLinkData paymentLinkData;
        try {
            paymentLinkData = payOS.getPaymentLinkInformation(order_id);
            if (paymentLinkData == null) {
                throw new RuntimeException("PaymentLinkData is null for order ID: " + order_id);
            }

            Optional<PayOSEntity> optionalPayOSEntity = payOSRepository.findByOrderCode(order_id);
            PayOSEntity payOSEntity = optionalPayOSEntity.orElseGet(() -> PayOSEntity.builder()
                    .paymentLinkId(paymentLinkData.getId())
                    .orderCode(paymentLinkData.getOrderCode())
                    .amount(paymentLinkData.getAmount())
                    .status(paymentLinkData.getStatus())
                    .canceledAt(paymentLinkData.getCanceledAt())
                    .cancellationReason(paymentLinkData.getCancellationReason())
                    .build());
            payOSRepository.save(payOSEntity);

            return paymentLinkData;

        } catch (Exception e) {
            throw new RuntimeException("Failed to process payment link for order ID: " + order_id, e);
        }
    }

    @Override
    public PayOSEntity getPayOS(Long id) {
        return payOSRepository.findByOrderCode(id).orElse(null);
    }

    @Override
    public String successPayOs(Long orderCode) {
        PayOSEntity p = getPayOS(orderCode);
        if (p != null) {
            p.setStatus("PAID");
            payOSRepository.save(p);
            return "success payment";
        }
        return "payment not found";
    }

    @Override
    public PaymentLinkData cancelLink(Long orderCode, String mess) {
        try {
            PaymentLinkData pd = payOS.cancelPaymentLink(orderCode, mess);
            PayOSEntity p = getPayOS(orderCode);
            if (p != null) {
                p.setStatus("CANCELED");
                payOSRepository.save(p);
            }
            return pd;
        } catch (Exception e) {
            log.error("Error while canceling payment link: ", e);
        }
        return null;
    }

    @Override
    public String confirmWebhook(String webhookUrl) {
        try {
            return payOS.confirmWebhook(webhookUrl);
        } catch (Exception e) {
            logger.error("Error confirming webhook URL: " + webhookUrl, e);
            return "Internal Server Error: " + e.getMessage();
        }
    }

    @Override
    public boolean verifyPaymentWebhookData(Webhook webhookBody) {
        try {
            WebhookData webhookData = payOS.verifyPaymentWebhookData(webhookBody);
            return webhookData != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void deletePayOSExprited(Long id) {

        Optional<PayOSEntity> payOS = payOSRepository.findById(id);
        if (payOS.isPresent()) {
            PayOSEntity payOSData = payOS.get();
            long currentTime = System.currentTimeMillis();
            long createdTime = payOSData.getExpireAt().longValue() * 1000;

            // quá 30 phút (1800000 milliseconds)
            if ((currentTime - createdTime) > 1000 && payOSData.getStatus().equals("PENDING")) {
                payOSRepository.deleteById(id);
            }
        }
    }

    @Scheduled(cron = "*/1 * * * * ?")
    public void deleteExpiredPayOSRecords() {
        List<PayOSEntity> payOSList = payOSRepository.findAll();

        for (PayOSEntity payOS : payOSList) {
            deletePayOSExprited(payOS.getId());
        }
    }

}