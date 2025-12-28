package com.group7.DMS.service;

import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Invoices;
import com.group7.DMS.repository.PaymentRepository;
import com.group7.DMS.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Override
	public Payments save(Payments payment) {
		return paymentRepository.save(payment);
	}

	@Override
	public Payments update(Payments payment) {
		return paymentRepository.save(payment);
	}

	@Override
	public void delete(int id) {
		paymentRepository.deleteById(id);
	}

	@Override
	public Payments findById(int id) {
		return paymentRepository.findById(id).orElse(null);
	}

	@Override
	public List<Payments> findAll() {
		return paymentRepository.findAll();
	}

	@Override
	public List<Payments> findByInvoiceId(int invoiceId) {
		return paymentRepository.findByInvoiceId(invoiceId);
	}

	@Override
	public List<Payments> findByInvoice(Invoices invoice) {
		return paymentRepository.findByInvoice(invoice);
	}

	@Override
	public List<Payments> findByStatus(Payments.PaymentStatus status) {
		return paymentRepository.findByStatus(status);
	}

	@Override
	public List<Payments> findByPaymentMethod(Payments.PaymentMethod method) {
		return paymentRepository.findByPaymentMethod(method);
	}

	@Override
	public List<Payments> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		return paymentRepository.findByDateRange(startDate, endDate);
	}

	@Override
	public BigDecimal calculateTotalPayments(LocalDateTime startDate, LocalDateTime endDate) {
		List<Payments> payments = paymentRepository.findByDateRange(startDate, endDate);
		return payments.stream().filter(p -> p.getStatus() == Payments.PaymentStatus.SUCCESS).map(Payments::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Override
	public Payments createPayment(int invoiceId, BigDecimal amount, Payments.PaymentMethod method,
			String transactionId) {
		Invoices invoice = invoiceRepository.findById(invoiceId)
				.orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));

		Payments payment = new Payments();
		payment.setInvoice(invoice);
		payment.setAmount(amount);
		payment.setPaymentMethod(method);
		payment.setTransactionId(transactionId);
		payment.setPaymentDate(LocalDateTime.now());
		payment.setStatus(Payments.PaymentStatus.SUCCESS);

		return paymentRepository.save(payment);
	}
}