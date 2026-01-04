package com.group7.DMS.service;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Invoices.InvoiceStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface InvoiceService {

	/**
	 * Tạo hóa đơn phí sinh hoạt cho PHÒNG (cách mới)
	 */
	Invoices createInvoiceForRoom(int roomId, int month, int year, int electricityUsage, int waterUsage);

	/**
	 * Tạo hóa đơn theo hợp đồng (cách cũ - giữ lại cho tương thích)
	 */
	Invoices createInvoiceForContract(int contractId, String invoiceNumber, LocalDate issueDate, LocalDate dueDate,
			int electricityUsage, int waterUsage);

	/**
	 * Tạo hóa đơn hàng loạt cho nhiều phòng
	 */
	Map<String, Object> createBulkInvoices(List<Integer> roomIds, int month, int year);

	/**
	 * Cập nhật hóa đơn
	 */
	Invoices updateInvoice(int invoiceId, Integer electricityUsage, Integer waterUsage);

	/**
	 * Đánh dấu đã thanh toán
	 */
	Invoices markAsPaid(int invoiceId);

	/**
	 * Cập nhật trạng thái hóa đơn
	 */
	Invoices updateInvoiceStatus(int invoiceId, InvoiceStatus status);

	/**
	 * Xóa hóa đơn
	 */
	void deleteInvoice(int invoiceId);

	/**
	 * Lấy chi tiết hóa đơn
	 */
	Invoices getInvoiceById(int invoiceId);

	/**
	 * Lấy tất cả hóa đơn
	 */
	List<Invoices> getAllInvoices();

	/**
	 * Lấy hóa đơn của phòng
	 */
	List<Invoices> getInvoicesByRoom(int roomId);

	/**
	 * Lấy hóa đơn theo hợp đồng
	 */
	List<Invoices> getInvoicesByContract(int contractId);

	/**
	 * Tìm kiếm hóa đơn
	 */
	Page<Invoices> searchInvoices(Integer buildingId, String roomNumber, InvoiceStatus status, Integer month,
			Integer year, int page, int size);

	/**
	 * Thống kê tổng hợp
	 */
	Map<String, Object> getInvoiceSummary();

	/**
	 * Cập nhật hóa đơn quá hạn
	 */
	int updateOverdueInvoices();

	/**
	 * Lấy hóa đơn theo tháng/năm
	 */
	List<Invoices> getInvoicesByMonthAndYear(int month, int year);

	/**
	 * Lấy hóa đơn theo student ID
	 */
	List<Invoices> findByStudentId(int studentId);

	/**
	 * Tìm hóa đơn theo ID
	 */
	Invoices findById(int id);

	/**
	 * Tính tổng tiền chưa thanh toán của sinh viên
	 */
	BigDecimal calculateTotalUnpaidAmount(int studentId);

	/**
	 * Xử lý thanh toán
	 */
	com.group7.DMS.entity.Payments processPayment(int invoiceId, BigDecimal amount, 
			com.group7.DMS.entity.Payments.PaymentMethod method, String transactionId);

}