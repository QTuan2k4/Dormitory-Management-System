package com.group7.DMS.service;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {

	// Các phương thức CRUD
	Invoices save(Invoices invoice);

	void delete(int id);

	Invoices update(Invoices invoice);

	Invoices findById(int id);

	Invoices findByInvoiceNumber(String invoiceNumber);

	List<Invoices> findAll();

	List<Invoices> findByStudentId(int studentId);

	// Tìm hóa đơn theo trạng thái với phân trang và lọc
	Page<Invoices> findInvoicesWithFilters(String search, Invoices.InvoiceStatus status, LocalDate fromDate,
			LocalDate toDate, Pageable pageable);

	// Phương thức tạo hóa đơn mới
	Invoices createInvoice(int contractId, String invoiceNumber, LocalDate issueDate, LocalDate dueDate,
			BigDecimal roomFee, BigDecimal electricityFee, BigDecimal waterFee, BigDecimal internetFee);

	// Phương thức thanh toán
	Payments processPayment(int invoiceId, BigDecimal amount, Payments.PaymentMethod method, String transactionId);

	// Các phương thức hỗ trợ tính toán
	BigDecimal calculateTotalAmount(BigDecimal roomFee, BigDecimal electricityFee, BigDecimal waterFee,
			BigDecimal internetFee);

	// Tính tổng tiền đã thanh toán hoặc chưa thanh toán cho sinh viên
	BigDecimal calculateTotalPaidAmount(int studentId);

	BigDecimal calculateTotalUnpaidAmount(int studentId);

	// Phương thức lấy các khoản thanh toán gần đây của sinh viên
	List<Payments> findRecentPaymentsByStudent(int studentId, int limit);

	List<Invoices> findByDateRange(LocalDate startDate, LocalDate endDate);
}
