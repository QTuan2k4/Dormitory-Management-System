package com.group7.DMS.service;

import com.group7.DMS.entity.Invoice;
import com.group7.DMS.entity.Payment;
import com.group7.DMS.entity.Contract;
import com.group7.DMS.repository.InvoiceRepository;
import com.group7.DMS.repository.PaymentRepository;
import com.group7.DMS.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private ContractRepository contractRepository;

    @Override
    public Invoice save(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice update(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public void delete(int id) {
        invoiceRepository.deleteById(id);
    }

    @Override
    public Invoice findById(int id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    @Override
    public Invoice findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber).orElse(null);
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    @Override
    public List<Invoice> findByStudentId(int studentId) {
        return invoiceRepository.findByStudentId(studentId);
    }

    @Override
    public List<Invoice> findByStudentIdAndStatus(int studentId, Invoice.InvoiceStatus status) {
        return invoiceRepository.findByStudentIdAndStatus(studentId, status);
    }

    @Override
    public List<Invoice> findByRoomId(int roomId) {
        return invoiceRepository.findByRoomId(roomId);
    }

    @Override
    public List<Invoice> findByStatus(Invoice.InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    @Override
    public List<Invoice> findOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now());
    }

    @Override
    public Invoice createInvoice(int contractId, String invoiceNumber, LocalDate issueDate, LocalDate dueDate,
                               BigDecimal roomFee, BigDecimal electricityFee, BigDecimal waterFee, BigDecimal internetFee) {
        Optional<Contract> contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isEmpty()) {
            throw new RuntimeException("Contract not found with id: " + contractId);
        }
        
        Invoice invoice = new Invoice();
        invoice.setContract(contractOpt.get());
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setIssueDate(issueDate);
        invoice.setDueDate(dueDate);
        invoice.setRoomFee(roomFee);
        invoice.setElectricityFee(electricityFee);
        invoice.setWaterFee(waterFee);
        invoice.setInternetFee(internetFee);
        invoice.setStatus(Invoice.InvoiceStatus.UNPAID);
        
        return invoiceRepository.save(invoice);
    }

    @Override
    public void markAsPaid(int invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            invoiceRepository.save(invoice);
        }
    }

    @Override
    public void markAsOverdue(int invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            invoice.setStatus(Invoice.InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
        }
    }

    @Override
    public Payment processPayment(int invoiceId, BigDecimal amount, Payment.PaymentMethod method, String transactionId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            throw new RuntimeException("Invoice not found with id: " + invoiceId);
        }
        
        Invoice invoice = invoiceOpt.get();
        
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setTransactionId(transactionId);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Mark invoice as paid
        markAsPaid(invoiceId);
        
        return savedPayment;
    }

    @Override
    public BigDecimal calculateTotalAmount(BigDecimal roomFee, BigDecimal electricityFee, BigDecimal waterFee, BigDecimal internetFee) {
        return roomFee.add(electricityFee).add(waterFee).add(internetFee);
    }

    @Override
    public List<Invoice> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> !invoice.getIssueDate().isBefore(startDate) && !invoice.getIssueDate().isAfter(endDate))
                .toList();
    }
}
