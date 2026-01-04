package com.group7.DMS.service.payment;

import com.group7.DMS.config.PaymentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ZaloPayService {

    @Autowired
    private PaymentConfig paymentConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createPaymentUrl(int invoiceId, long amount, String description) {
        try {
            String appTransId = getCurrentDateString("yyMMdd") + "_" + invoiceId + "_" + System.currentTimeMillis();
            long appTime = System.currentTimeMillis();
            
            // Embed data chứa invoiceId để callback có thể lấy
            Map<String, Object> embedData = new HashMap<>();
            embedData.put("invoiceId", invoiceId);
            embedData.put("redirecturl", paymentConfig.getZalopayCallbackUrl());
            String embedDataStr = objectMapper.writeValueAsString(embedData);

            // Item - có thể để trống hoặc thêm thông tin hóa đơn
            String item = "[]";

            // Build raw data for MAC
            String rawData = paymentConfig.getZalopayAppId() + "|" + appTransId + "|" 
                    + "user_dms" + "|" + amount + "|" + appTime + "|" 
                    + embedDataStr + "|" + item;
            
            String mac = hmacSHA256(paymentConfig.getZalopayKey1(), rawData);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("app_id", Integer.parseInt(paymentConfig.getZalopayAppId()));
            requestBody.put("app_user", "user_dms");
            requestBody.put("app_trans_id", appTransId);
            requestBody.put("app_time", appTime);
            requestBody.put("amount", amount);
            requestBody.put("item", item);
            requestBody.put("description", description);
            requestBody.put("embed_data", embedDataStr);
            requestBody.put("bank_code", "");
            requestBody.put("mac", mac);
            requestBody.put("callback_url", paymentConfig.getZalopayCallbackUrl());

            // Send request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            StringBuilder formBody = new StringBuilder();
            for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                if (formBody.length() > 0) formBody.append("&");
                formBody.append(entry.getKey()).append("=").append(entry.getValue());
            }

            HttpEntity<String> entity = new HttpEntity<>(formBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    paymentConfig.getZalopayEndpoint(), entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
                Integer returnCode = (Integer) responseBody.get("return_code");
                if (returnCode != null && returnCode == 1) {
                    return (String) responseBody.get("order_url");
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error creating ZaloPay payment: " + e.getMessage(), e);
        }
    }

    public boolean validateCallback(String data, String requestMac) {
        try {
            String calculatedMac = hmacSHA256(paymentConfig.getZalopayKey2(), data);
            return calculatedMac.equals(requestMac);
        } catch (Exception e) {
            return false;
        }
    }

    public int extractInvoiceId(String appTransId) {
        // Format: yyMMdd_invoiceId_timestamp
        String[] parts = appTransId.split("_");
        if (parts.length >= 2) {
            return Integer.parseInt(parts[1]);
        }
        return -1;
    }

    private String getCurrentDateString(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
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
