package com.group7.DMS.service;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {
    Invoices save(Invoices invoice);
    Invoices update(Invoices invoice);
    void delete(int id);
    Invoices findById(int id);
    Invoices findByInvoiceNumber(String invoiceNumber);
    List<Invoices> findAll();
    List<Invoices> findByStudentId(int studentId);
    List<Invoices> findByStudentIdAndStatus(int studentId, Invoices.InvoiceStatus status);
    List<Invoices> findByRoomId(int roomId);
    List<Invoices> findByStatus(Invoices.InvoiceStatus status);
    List<Invoices> findOverdueInvoices();
    Invoices createInvoice(int contractId, String invoiceNumber, LocalDate issueDate, LocalDate dueDate,
                        BigDecimal roomFee, BigDecimal electricityFee, BigDecimal waterFee, BigDecimal internetFee);
    void markAsPaid(int invoiceId);
    void markAsOverdue(int invoiceId);
    Payments processPayment(int invoiceId, BigDecimal amount, Payments.PaymentMethod method, String transactionId);
    BigDecimal calculateTotalAmount(BigDecimal roomFee, BigDecimal electricityFee, BigDecimal waterFee, BigDecimal internetFee);
    List<Invoices> findByDateRange(LocalDate startDate, LocalDate endDate);
    BigDecimal calculateTotalPaidAmount(int studentId);
    BigDecimal calculateTotalUnpaidAmount(int studentId);
    List<Payments> findRecentPaymentsByStudent(int studentId, int limit);
}
