package com.group7.DMS.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "invoice.pricing")
public class InvoicePricingConfig {

	// Giá điện (VNĐ/kWh)
	private BigDecimal electricityPricePerKwh = new BigDecimal("3500");

	// Giá nước (VNĐ/m³)
	private BigDecimal waterPricePerM3 = new BigDecimal("20000");

	// Giá Internet (VNĐ/tháng) - cố định
	private BigDecimal internetFeePerMonth = new BigDecimal("100000");

	// Getters and Setters
	public BigDecimal getElectricityPricePerKwh() {
		return electricityPricePerKwh;
	}

	public void setElectricityPricePerKwh(BigDecimal electricityPricePerKwh) {
		this.electricityPricePerKwh = electricityPricePerKwh;
	}

	public BigDecimal getWaterPricePerM3() {
		return waterPricePerM3;
	}

	public void setWaterPricePerM3(BigDecimal waterPricePerM3) {
		this.waterPricePerM3 = waterPricePerM3;
	}

	public BigDecimal getInternetFeePerMonth() {
		return internetFeePerMonth;
	}

	public void setInternetFeePerMonth(BigDecimal internetFeePerMonth) {
		this.internetFeePerMonth = internetFeePerMonth;
	}
}