package com.group7.DMS.service;

import com.group7.DMS.entity.Invoice;
import com.group7.DMS.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {
    Invoice save(Invoice invoice);
    Invoice update(Invoice invoice);
    void delete(int id);
    Invoice findById(int id);
    Invoice findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findAll();
    List<Invoice> findByStudentId(int studentId);
    List<Invoice> findByStudentIdAndStatus(int studentId, Invoice.InvoiceStatus status);
    List<Invoice> findByRoomId(int roomId);
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    List<Invoice> findOverdueInvoices();
    Invoice createInvoice(int contractId, String invoiceNumber, LocalDate issueDate, LocalDate dueDate,
                        BigDecimal roomFee, BigDecimal electricityFee, BigDecimal waterFee, BigDecimal internetFee);
    void markAsPaid(int invoiceId);
    void markAsOverdue(int invoiceId);
    Payment processPayment(int invoiceId, BigDecimal amount, Payment.PaymentMethod method, String transactionId);
    BigDecimal calculateTotalAmount(BigDecimal roomFee, BigDecimal electricityFee, BigDecimal waterFee, BigDecimal internetFee);
    List<Invoice> findByDateRange(LocalDate startDate, LocalDate endDate);
}
