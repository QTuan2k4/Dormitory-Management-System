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

    // ===== MoMo Configuration =====
    @Value("${momo.partner-code:}")
    private String momoPartnerCode;
    
    @Value("${momo.access-key:}")
    private String momoAccessKey;
    
    @Value("${momo.secret-key:}")
    private String momoSecretKey;
    
    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String momoEndpoint;
    
    @Value("${momo.return-url:http://localhost:8080/payment/momo/callback}")
    private String momoReturnUrl;
    
    @Value("${momo.notify-url:http://localhost:8080/payment/momo/ipn}")
    private String momoNotifyUrl;

    // ===== ZaloPay Configuration =====
    @Value("${zalopay.app-id:}")
    private String zalopayAppId;
    
    @Value("${zalopay.key1:}")
    private String zalopayKey1;
    
    @Value("${zalopay.key2:}")
    private String zalopayKey2;
    
    @Value("${zalopay.endpoint:https://sb-openapi.zalopay.vn/v2/create}")
    private String zalopayEndpoint;
    
    @Value("${zalopay.callback-url:http://localhost:8080/payment/zalopay/callback}")
    private String zalopayCallbackUrl;

    // Getters
    public String getVnpTmnCode() { return vnpTmnCode; }
    public String getVnpHashSecret() { return vnpHashSecret; }
    public String getVnpUrl() { return vnpUrl; }
    public String getVnpReturnUrl() { return vnpReturnUrl; }
    
    public String getMomoPartnerCode() { return momoPartnerCode; }
    public String getMomoAccessKey() { return momoAccessKey; }
    public String getMomoSecretKey() { return momoSecretKey; }
    public String getMomoEndpoint() { return momoEndpoint; }
    public String getMomoReturnUrl() { return momoReturnUrl; }
    public String getMomoNotifyUrl() { return momoNotifyUrl; }
    
    public String getZalopayAppId() { return zalopayAppId; }
    public String getZalopayKey1() { return zalopayKey1; }
    public String getZalopayKey2() { return zalopayKey2; }
    public String getZalopayEndpoint() { return zalopayEndpoint; }
    public String getZalopayCallbackUrl() { return zalopayCallbackUrl; }
}
