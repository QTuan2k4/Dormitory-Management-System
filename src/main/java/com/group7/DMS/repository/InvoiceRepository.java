package com.group7.DMS.repository;

import com.group7.DMS.entity.Invoice;
import com.group7.DMS.entity.Invoice.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByContractId(int contractId);

    List<Invoice> findByStatus(InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.contract.student.id = :studentId")
    List<Invoice> findByStudentId(@Param("studentId") int studentId);

    @Query("SELECT i FROM Invoice i WHERE i.contract.student.id = :studentId AND i.status = :status")
    List<Invoice> findByStudentIdAndStatus(@Param("studentId") int studentId,
                                           @Param("status") InvoiceStatus status);

    // ✅ Sửa lỗi Enum: dùng Enum thay vì String literal
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :date AND i.status = com.group7.DMS.entity.Invoice$InvoiceStatus.UNPAID")
    List<Invoice> findOverdueInvoices(@Param("date") LocalDate date);

    @Query("SELECT i FROM Invoice i WHERE i.contract.room.id = :roomId")
    List<Invoice> findByRoomId(@Param("roomId") int roomId);
}
