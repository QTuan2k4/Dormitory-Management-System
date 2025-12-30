package com.group7.DMS.scheduler;

import com.group7.DMS.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InvoiceScheduler {

	private static final Logger logger = LoggerFactory.getLogger(InvoiceScheduler.class);

	@Autowired
	private InvoiceService invoiceService;

	/**
	 * Tự động cập nhật trạng thái hóa đơn quá hạn Chạy mỗi ngày lúc 00:00 (nửa đêm)
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	public void updateOverdueInvoicesDaily() {
		logger.info("Bắt đầu kiểm tra và cập nhật hóa đơn quá hạn...");

		try {
			int count = invoiceService.updateOverdueInvoices();
			logger.info("Đã cập nhật {} hóa đơn sang trạng thái OVERDUE", count);
		} catch (Exception e) {
			logger.error("Lỗi khi cập nhật hóa đơn quá hạn: {}", e.getMessage(), e);
		}
	}
}