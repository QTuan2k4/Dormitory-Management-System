package com.group7.DMS.repository;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Invoices.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoices, Integer> {

	// Tìm kiếm hóa đơn theo số hóa đơn
	Optional<Invoices> findByInvoiceNumber(String invoiceNumber);

	// Tìm hóa đơn theo roomId
	List<Invoices> findByRoomId(int roomId);

	// Tìm hóa đơn theo contractId
	List<Invoices> findByContractId(int contractId);

	Optional<Invoices> findByRoomIdAndMonthAndYear(int roomId, int month, int year);

	boolean existsByInvoiceNumber(String invoiceNumber);

	boolean existsByRoomIdAndMonthAndYear(int roomId, int month, int year);

	@Query("SELECT i FROM Invoices i " + "JOIN i.room r " + "JOIN r.building b "
			+ "WHERE (:buildingId IS NULL OR b.id = :buildingId) "
			+ "AND (:roomNumber IS NULL OR r.roomNumber LIKE %:roomNumber%) "
			+ "AND (:status IS NULL OR i.status = :status) " + "AND (:month IS NULL OR MONTH(i.issueDate) = :month) "
			+ "AND (:year IS NULL OR YEAR(i.issueDate) = :year)")
	Page<Invoices> searchInvoices(@Param("buildingId") Integer buildingId, @Param("roomNumber") String roomNumber,
			@Param("status") InvoiceStatus status, @Param("month") Integer month, @Param("year") Integer year,
			Pageable pageable);

	@Query("SELECT COALESCE(SUM(i.electricityFee + i.waterFee + i.internetFee), 0) "
			+ "FROM Invoices i WHERE i.status = :status")
	BigDecimal sumTotalAmountByStatus(@Param("status") InvoiceStatus status);

	long countByStatus(InvoiceStatus status);

	@Query("SELECT i FROM Invoices i WHERE i.status = 'UNPAID' AND i.dueDate < :currentDate")
	List<Invoices> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

	List<Invoices> findByMonthAndYear(int month, int year);

}