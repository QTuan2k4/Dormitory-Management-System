package com.group7.DMS.service.payment;

import com.group7.DMS.config.PaymentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MoMoService {

    @Autowired
    private PaymentConfig paymentConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createPaymentUrl(int invoiceId, long amount, String orderInfo) {
        try {
            String orderId = "DMS_" + invoiceId + "_" + System.currentTimeMillis();
            String requestId = orderId;
            String extraData = String.valueOf(invoiceId);

            // Build raw signature
            String rawSignature = "accessKey=" + paymentConfig.getMomoAccessKey()
                    + "&amount=" + amount
                    + "&extraData=" + extraData
                    + "&ipnUrl=" + paymentConfig.getMomoNotifyUrl()
                    + "&orderId=" + orderId
                    + "&orderInfo=" + orderInfo
                    + "&partnerCode=" + paymentConfig.getMomoPartnerCode()
                    + "&redirectUrl=" + paymentConfig.getMomoReturnUrl()
                    + "&requestId=" + requestId
                    + "&requestType=captureWallet";

            String signature = hmacSHA256(paymentConfig.getMomoSecretKey(), rawSignature);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", paymentConfig.getMomoPartnerCode());
            requestBody.put("accessKey", paymentConfig.getMomoAccessKey());
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", paymentConfig.getMomoReturnUrl());
            requestBody.put("ipnUrl", paymentConfig.getMomoNotifyUrl());
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", "captureWallet");
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");

            // Send request to MoMo
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    paymentConfig.getMomoEndpoint(), entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
                if (responseBody.containsKey("payUrl")) {
                    return (String) responseBody.get("payUrl");
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error creating MoMo payment: " + e.getMessage(), e);
        }
    }

    public boolean validateCallback(Map<String, String> params) {
        try {
            String receivedSignature = params.get("signature");
            if (receivedSignature == null) return false;

            String rawSignature = "accessKey=" + paymentConfig.getMomoAccessKey()
                    + "&amount=" + params.get("amount")
                    + "&extraData=" + params.get("extraData")
                    + "&message=" + params.get("message")
                    + "&orderId=" + params.get("orderId")
                    + "&orderInfo=" + params.get("orderInfo")
                    + "&orderType=" + params.get("orderType")
                    + "&partnerCode=" + params.get("partnerCode")
                    + "&payType=" + params.get("payType")
                    + "&requestId=" + params.get("requestId")
                    + "&responseTime=" + params.get("responseTime")
                    + "&resultCode=" + params.get("resultCode")
                    + "&transId=" + params.get("transId");

            String calculatedSignature = hmacSHA256(paymentConfig.getMomoSecretKey(), rawSignature);
            return calculatedSignature.equals(receivedSignature);
        } catch (Exception e) {
            return false;
        }
    }

    public int extractInvoiceId(String extraData) {
        return Integer.parseInt(extraData);
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac256.init(secretKey);
            byte[] result = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA256", e);
        }
    }
}
