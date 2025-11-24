package com.group7.DMS.service;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Contracts;
import com.group7.DMS.entity.Invoices.InvoiceStatus;
import com.group7.DMS.repository.InvoiceRepository;
import com.group7.DMS.repository.PaymentRepository;
import com.group7.DMS.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public Invoices save(Invoices invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoices update(Invoices invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public void delete(int id) {
        invoiceRepository.deleteById(id);
    }

    @Override
    public Invoices findById(int id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    @Override
    public Invoices findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber).orElse(null);
    }

    @Override
    public List<Invoices> findAll() {
        return invoiceRepository.findAll();
    }

    @Override
    public List<Invoices> findByStudentId(int studentId) {
        return invoiceRepository.findByStudentId(studentId);
    }

    @Override
    public List<Invoices> findByStudentIdAndStatus(int studentId, Invoices.InvoiceStatus status) {
        return invoiceRepository.findByStudentIdAndStatus(studentId, status);
    }

    @Override
    public List<Invoices> findByRoomId(int roomId) {
        return invoiceRepository.findByRoomId(roomId);
    }

    @Override
    public List<Invoices> findByStatus(Invoices.InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    @Override
    public List<Invoices> findOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now(), Invoices.InvoiceStatus.UNPAID);
    }

    @Override
    public Invoices createInvoice(int contractId, String invoiceNumber, LocalDate issueDate, LocalDate dueDate,
                               BigDecimal roomFee, BigDecimal electricityFee, BigDecimal waterFee, BigDecimal internetFee) {
        Optional<Contracts> contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isEmpty()) {
            throw new RuntimeException("Contract not found with id: " + contractId);
        }
        
        Invoices invoice = new Invoices();
        invoice.setContract(contractOpt.get());
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setIssueDate(issueDate);
        invoice.setDueDate(dueDate);
        invoice.setRoomFee(roomFee);
        invoice.setElectricityFee(electricityFee);
        invoice.setWaterFee(waterFee);
        invoice.setInternetFee(internetFee);
        invoice.setStatus(Invoices.InvoiceStatus.UNPAID);
        
        return invoiceRepository.save(invoice);
    }

    @Override
    public void markAsPaid(int invoiceId) {
        Optional<Invoices> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoices invoice = invoiceOpt.get();
            invoice.setStatus(Invoices.InvoiceStatus.PAID);
            invoiceRepository.save(invoice);
        }
    }

    @Override
    public void markAsOverdue(int invoiceId) {
        Optional<Invoices> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoices invoice = invoiceOpt.get();
            invoice.setStatus(Invoices.InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
        }
    }

    @Override
    public Payments processPayment(int invoiceId, BigDecimal amount, Payments.PaymentMethod method, String transactionId) {
        Optional<Invoices> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            throw new RuntimeException("Invoice not found with id: " + invoiceId);
        }
        
        Invoices invoice = invoiceOpt.get();
        
        Payments payment = new Payments();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setTransactionId(transactionId);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(Payments.PaymentStatus.SUCCESS);
        
        Payments savedPayment = paymentRepository.save(payment);
        
        // Mark invoice as paid
        markAsPaid(invoiceId);
        
        return savedPayment;
    }

    @Override
    public BigDecimal calculateTotalAmount(BigDecimal roomFee, BigDecimal electricityFee, BigDecimal waterFee, BigDecimal internetFee) {
        return roomFee.add(electricityFee).add(waterFee).add(internetFee);
    }

    @Override
    public List<Invoices> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> !invoice.getIssueDate().isBefore(startDate) && !invoice.getIssueDate().isAfter(endDate))
                .toList();
    }
    
    @Override
    public BigDecimal calculateTotalPaidAmount(int studentId) {
        // Lấy tất cả hóa đơn ĐÃ THANH TOÁN của sinh viên
        List<Invoices> paidInvoices = invoiceRepository.findByStudentIdAndStatus(studentId, InvoiceStatus.PAID);
        return paidInvoices.stream()
            .map(Invoices::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public BigDecimal calculateTotalUnpaidAmount(int studentId) {
        // Lấy tất cả hóa đơn CHƯA THANH TOÁN
        List<Invoices> unpaidInvoices = invoiceRepository.findByStudentIdAndStatus(studentId, InvoiceStatus.UNPAID);        
        // Lấy tất cả hóa đơn QUÁ HẠN
        List<Invoices> overdueInvoices = invoiceRepository.findByStudentIdAndStatus(studentId, InvoiceStatus.OVERDUE);
        // Cộng tổng tiền của cả hai danh sách
        BigDecimal totalUnpaid = unpaidInvoices.stream()
            .map(Invoices::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalOverdue = overdueInvoices.stream()
            .map(Invoices::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        return totalUnpaid.add(totalOverdue);
    }
    
    @Override
    public List<Payments> findRecentPaymentsByStudent(int studentId, int limit) {
        List<Invoices> paidInvoices = invoiceRepository.findByStudentIdAndStatus(studentId, InvoiceStatus.PAID);       
        // Lấy tất cả Payments cho các hóa đơn này
        List<Payments> allPayments = paidInvoices.stream()
            .flatMap(invoice -> paymentRepository.findByInvoice(invoice).stream()) 
            .toList();
        // Sắp xếp và giới hạn kết quả
        return allPayments.stream()
            .sorted((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate())) // Sắp xếp giảm dần theo ngày
            .limit(limit)
            .toList();
    }
    
}