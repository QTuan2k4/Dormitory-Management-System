package com.group7.DMS.repository;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Invoices.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoices, Integer> {
    
    Optional<Invoices> findByInvoiceNumber(String invoiceNumber);

    List<Invoices> findByContractId(int contractId);

    List<Invoices> findByStatus(InvoiceStatus status);

    @Query("SELECT i FROM Invoices i WHERE i.contract.student.id = :studentId")
    List<Invoices> findByStudentId(@Param("studentId") int studentId);

    @Query("SELECT i FROM Invoices i WHERE i.contract.student.id = :studentId AND i.status = :status")
    List<Invoices> findByStudentIdAndStatus(@Param("studentId") int studentId,
                                           @Param("status") InvoiceStatus status);

    @Query("SELECT i FROM Invoices i WHERE i.dueDate < :date AND i.status = :status")
    List<Invoices> findOverdueInvoices(@Param("date") LocalDate date, 
                                       @Param("status") InvoiceStatus status);

    @Query("SELECT i FROM Invoices i WHERE i.contract.room.id = :roomId")
    List<Invoices> findByRoomId(@Param("roomId") int roomId);
}