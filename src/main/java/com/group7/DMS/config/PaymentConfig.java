package com.group7.DMS.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    // ===== VNPay Configuration =====
    @Value("${vnpay.tmn-code:}")
    private String vnpTmnCode;
    
    @Value("${vnpay.hash-secret:}")
    private String vnpHashSecret;
    
    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpUrl;
    
    @Value("${vnpay.return-url:http://localhost:8080/payment/vnpay/callback}")
    private String vnpReturnUrl;

    // Getters
    public String getVnpTmnCode() { return vnpTmnCode; }
    public String getVnpHashSecret() { return vnpHashSecret; }
    public String getVnpUrl() { return vnpUrl; }
    public String getVnpReturnUrl() { return vnpReturnUrl; }
}
