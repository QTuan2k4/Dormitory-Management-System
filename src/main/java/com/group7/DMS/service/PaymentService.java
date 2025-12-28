package com.group7.DMS.service;

import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Invoices;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
	Payments save(Payments payment);

	Payments update(Payments payment);

	void delete(int id);

	Payments findById(int id);

	List<Payments> findAll();

	List<Payments> findByInvoiceId(int invoiceId);

	List<Payments> findByInvoice(Invoices invoice);

	List<Payments> findByStatus(Payments.PaymentStatus status);

	List<Payments> findByPaymentMethod(Payments.PaymentMethod method);

	List<Payments> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

	BigDecimal calculateTotalPayments(LocalDateTime startDate, LocalDateTime endDate);

	Payments createPayment(int invoiceId, BigDecimal amount, Payments.PaymentMethod method, String transactionId);
}