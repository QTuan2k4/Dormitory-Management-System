package com.group7.DMS.repository;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Invoices.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoices, Integer> {

	Optional<Invoices> findByInvoiceNumber(String invoiceNumber);

	List<Invoices> findByContractId(int contractId);

	@Query("SELECT i FROM Invoices i WHERE i.contract.student.id = :studentId")
	List<Invoices> findByStudentId(@Param("studentId") int studentId);

	@Query("SELECT i FROM Invoices i WHERE i.contract.student.id = :studentId AND i.status = :status")
	List<Invoices> findByStudentIdAndStatus(@Param("studentId") int studentId, @Param("status") InvoiceStatus status);

	@Query("SELECT i FROM Invoices i WHERE i.dueDate < :date AND i.status = :status")
	List<Invoices> findOverdueInvoices(@Param("date") LocalDate date, @Param("status") InvoiceStatus status);

	@Query("SELECT i FROM Invoices i WHERE i.contract.room.id = :roomId")
	List<Invoices> findByRoomId(@Param("roomId") int roomId);

	// Cải thiện câu truy vấn tìm hóa đơn theo trạng thái và ngày tháng
	@Query("SELECT i FROM Invoices i WHERE "
			+ "(LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.contract.student.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) "
			+ "AND (:status IS NULL OR i.status = :status) " + "AND (:fromDate IS NULL OR i.issueDate >= :fromDate) "
			+ "AND (:toDate IS NULL OR i.issueDate <= :toDate)")
	Page<Invoices> findInvoicesWithFilters(@Param("search") String search, @Param("status") InvoiceStatus status,
			@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);
}
