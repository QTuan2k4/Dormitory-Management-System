package com.group7.DMS.repository;

import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Invoices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, Integer> {

	List<Payments> findByStatus(Payments.PaymentStatus status);

	List<Payments> findByInvoice(Invoices invoice);

	List<Payments> findByPaymentMethod(Payments.PaymentMethod method);

	@Query("SELECT p FROM Payments p WHERE p.invoice.id = :invoiceId")
	List<Payments> findByInvoiceId(@Param("invoiceId") int invoiceId);

	@Query("SELECT p FROM Payments p WHERE p.invoice.contract.student.id = :studentId")
	List<Payments> findByStudentId(@Param("studentId") int studentId);

	@Query("SELECT p FROM Payments p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
	List<Payments> findByPaymentDateBetween(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	@Query("SELECT p FROM Payments p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
	List<Payments> findByDateRange(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	@Query("SELECT p FROM Payments p WHERE p.status = com.group7.DMS.entity.Payments$PaymentStatus.SUCCESS AND p.paymentDate BETWEEN :startDate AND :endDate")
	List<Payments> findSuccessfulPaymentsBetween(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);
}
