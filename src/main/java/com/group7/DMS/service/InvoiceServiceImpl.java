package com.group7.DMS.service;

import com.group7.DMS.config.InvoicePricingConfig;
import com.group7.DMS.entity.Contracts;
import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Invoices.InvoiceStatus;
import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Rooms;
import com.group7.DMS.repository.ContractRepository;
import com.group7.DMS.repository.InvoiceRepository;
import com.group7.DMS.repository.PaymentRepository;
import com.group7.DMS.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private ContractRepository contractRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private InvoicePricingConfig pricingConfig;

	@Override
	public Invoices createInvoiceForRoom(int roomId, int month, int year, int electricityUsage, int waterUsage) {
		if (month < 1 || month > 12) {
			throw new RuntimeException("Tháng phải từ 1 đến 12");
		}
		if (year < 2000 || year > 2100) {
			throw new RuntimeException("Năm không hợp lệ");
		}
		if (electricityUsage < 0) {
			throw new RuntimeException("Số điện không được âm");
		}
		if (waterUsage < 0) {
			throw new RuntimeException("Số nước không được âm");
		}

		Rooms room = roomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Phòng không tồn tại với ID: " + roomId));

		Optional<Invoices> existing = invoiceRepository.findByRoomIdAndMonthAndYear(roomId, month, year);
		if (existing.isPresent()) {
			throw new RuntimeException(
					"Đã tồn tại hóa đơn cho phòng " + room.getRoomNumber() + " tháng " + month + "/" + year);
		}

		Invoices invoice = new Invoices();
		invoice.setRoom(room);

		List<Contracts> activeContracts = contractRepository.findActiveContractsByRoom(roomId);
		if (!activeContracts.isEmpty()) {
			invoice.setContract(activeContracts.get(0));
		}

		String invoiceNumber = generateInvoiceNumber(roomId, month, year);
		invoice.setInvoiceNumber(invoiceNumber);
		invoice.setMonth(month);
		invoice.setYear(year);

		LocalDate issueDate = LocalDate.now();
		invoice.setIssueDate(issueDate);

		// YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate dueDate = issueDate.plusDays(15);
		invoice.setDueDate(dueDate);

		invoice.setElectricityUsage(electricityUsage);
		invoice.setWaterUsage(waterUsage);

		// Tính tiền phòng theo tháng (giá phòng/năm chia 12)
		BigDecimal roomFee = BigDecimal.ZERO;
		if (room.getPricePerYear() != null && room.getPricePerYear().compareTo(BigDecimal.ZERO) > 0) {
			roomFee = room.getPricePerYear().divide(new BigDecimal(12), 0, java.math.RoundingMode.CEILING);
		}
		invoice.setRoomFee(roomFee);

		BigDecimal electricityFee = pricingConfig.getElectricityPricePerKwh()
				.multiply(new BigDecimal(electricityUsage));
		invoice.setElectricityFee(electricityFee != null ? electricityFee : BigDecimal.ZERO);

		BigDecimal waterFee = pricingConfig.getWaterPricePerM3().multiply(new BigDecimal(waterUsage));
		invoice.setWaterFee(waterFee != null ? waterFee : BigDecimal.ZERO);

		BigDecimal internetFee = pricingConfig.getInternetFeePerMonth();
		invoice.setInternetFee(internetFee != null ? internetFee : BigDecimal.ZERO);

		invoice.setStatus(InvoiceStatus.UNPAID);

		return invoiceRepository.save(invoice);
	}

	@Override
	public Invoices createInvoiceForContract(int contractId, String invoiceNumber, LocalDate issueDate,
			LocalDate dueDate, int electricityUsage, int waterUsage) {

		Contracts contract = contractRepository.findById(contractId)
				.orElseThrow(() -> new RuntimeException("Contract không tồn tại với ID: " + contractId));

		if (contract.getStatus() != Contracts.ContractStatus.ACTIVE) {
			throw new RuntimeException("Contract không ở trạng thái ACTIVE");
		}

		if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
			throw new RuntimeException("Số hóa đơn đã tồn tại: " + invoiceNumber);
		}

		if (dueDate.isBefore(issueDate)) {
			throw new RuntimeException("Ngày đến hạn phải sau ngày phát hành");
		}

		Invoices invoice = new Invoices();
		invoice.setContract(contract);
		invoice.setRoom(contract.getRoom());
		invoice.setInvoiceNumber(invoiceNumber);
		invoice.setIssueDate(issueDate);
		invoice.setDueDate(dueDate);
		invoice.setStatus(InvoiceStatus.UNPAID);

		// Tính tiền phòng theo tháng
		BigDecimal roomFee = BigDecimal.ZERO;
		if (contract.getRoom() != null && contract.getRoom().getPricePerYear() != null
				&& contract.getRoom().getPricePerYear().compareTo(BigDecimal.ZERO) > 0) {
			roomFee = contract.getRoom().getPricePerYear().divide(new BigDecimal(12), 0,
					java.math.RoundingMode.CEILING);
		}
		invoice.setRoomFee(roomFee);

		BigDecimal electricityFee = pricingConfig.getElectricityPricePerKwh()
				.multiply(new BigDecimal(electricityUsage));
		invoice.setElectricityFee(electricityFee);

		BigDecimal waterFee = pricingConfig.getWaterPricePerM3().multiply(new BigDecimal(waterUsage));
		invoice.setWaterFee(waterFee);

		invoice.setInternetFee(pricingConfig.getInternetFeePerMonth());

		return invoiceRepository.save(invoice);
	}

	@Override
	public Map<String, Object> createBulkInvoices(List<Integer> roomIds, int month, int year) {
		Map<String, Object> result = new HashMap<>();
		int successCount = 0;
		int failCount = 0;
		StringBuilder errors = new StringBuilder();

		for (Integer roomId : roomIds) {
			try {
				createInvoiceForRoom(roomId, month, year, 0, 0);
				successCount++;
			} catch (Exception e) {
				failCount++;
				errors.append("Phòng ID ").append(roomId).append(": ").append(e.getMessage()).append("\n");
			}
		}

		result.put("successCount", successCount);
		result.put("failCount", failCount);
		result.put("errors", errors.toString());

		return result;
	}

	@Override
	public Invoices updateInvoice(int invoiceId, Integer electricityUsage, Integer waterUsage) {
		Invoices invoice = invoiceRepository.findById(invoiceId)
				.orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

		if (invoice.getStatus() == InvoiceStatus.PAID) {
			throw new RuntimeException("Không thể chỉnh sửa hóa đơn đã thanh toán");
		}

		if (electricityUsage != null) {
			BigDecimal electricityFee = pricingConfig.getElectricityPricePerKwh()
					.multiply(new BigDecimal(electricityUsage));
			invoice.setElectricityFee(electricityFee);
		}

		if (waterUsage != null) {
			BigDecimal waterFee = pricingConfig.getWaterPricePerM3().multiply(new BigDecimal(waterUsage));
			invoice.setWaterFee(waterFee);
		}

		return invoiceRepository.save(invoice);
	}

	@Override
	public Invoices markAsPaid(int invoiceId) {
		return updateInvoiceStatus(invoiceId, InvoiceStatus.PAID);
	}

	@Override
	public Invoices updateInvoiceStatus(int invoiceId, InvoiceStatus status) {
		Invoices invoice = invoiceRepository.findById(invoiceId)
				.orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

		invoice.setStatus(status);
		return invoiceRepository.save(invoice);
	}

	@Override
	public void deleteInvoice(int invoiceId) {
		Invoices invoice = invoiceRepository.findById(invoiceId)
				.orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

		if (invoice.getStatus() == InvoiceStatus.PAID) {
			throw new RuntimeException("Không thể xóa hóa đơn đã thanh toán");
		}

		if (invoice.getStatus() == InvoiceStatus.OVERDUE) {
			throw new RuntimeException("Không thể xóa hóa đơn đã quá hạn");
		}

		invoiceRepository.delete(invoice);
	}

	@Override
	public Invoices getInvoiceById(int invoiceId) {
		return invoiceRepository.findById(invoiceId)
				.orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại với ID: " + invoiceId));
	}

	@Override
	public List<Invoices> getAllInvoices() {
		return invoiceRepository.findAll();
	}

	@Override
	public List<Invoices> getInvoicesByRoom(int roomId) {
		return invoiceRepository.findByRoomId(roomId);
	}

	@Override
	public List<Invoices> getInvoicesByContract(int contractId) {
		return invoiceRepository.findByContractId(contractId);
	}

	@Override
	public Page<Invoices> searchInvoices(Integer buildingId, String roomNumber, InvoiceStatus status, Integer month,
			Integer year, int page, int size) {

		Sort sort = Sort.by("year").descending().and(Sort.by("month").descending())
				.and(Sort.by("issueDate").descending());
		Pageable pageable = PageRequest.of(page, size, sort);

		return invoiceRepository.searchInvoices(buildingId, roomNumber, status, month, year, pageable);
	}

	@Override
	public Map<String, Object> getInvoiceSummary() {
		Map<String, Object> summary = new HashMap<>();
		LocalDate today = LocalDate.now();

		// Lấy tất cả hóa đơn và tính toán
		List<Invoices> allInvoices = invoiceRepository.findAll();

		BigDecimal totalPaid = BigDecimal.ZERO;
		BigDecimal totalUnpaid = BigDecimal.ZERO;
		BigDecimal totalOverdue = BigDecimal.ZERO;
		long countPaid = 0;
		long countUnpaid = 0;
		long countOverdue = 0;

		for (Invoices invoice : allInvoices) {
			// Sử dụng getTotalAmount() thay vì getLivingTotalAmount() để tính đầy đủ
			BigDecimal amount = invoice.getTotalAmount();

			if (invoice.getStatus() == InvoiceStatus.PAID) {
				totalPaid = totalPaid.add(amount);
				countPaid++;
			} else {
				// Chưa thanh toán
				// Kiểm tra xem đã quá hạn chưa
				if (invoice.getDueDate().isBefore(today)) {
					// Quá hạn
					totalOverdue = totalOverdue.add(amount);
					countOverdue++;
				} else {
					// Chưa quá hạn
					totalUnpaid = totalUnpaid.add(amount);
					countUnpaid++;
				}
			}
		}

		summary.put("totalPaidAmount", totalPaid);
		summary.put("totalUnpaidAmount", totalUnpaid);
		summary.put("totalOverdueAmount", totalOverdue);
		summary.put("countPaid", countPaid);
		summary.put("countUnpaid", countUnpaid);
		summary.put("countOverdue", countOverdue);
		summary.put("totalInvoices", countPaid + countUnpaid + countOverdue);

		return summary;
	}

	@Override
	@Transactional
	public int updateOverdueInvoices() {
		LocalDate today = LocalDate.now();

		// Tìm tất cả HĐ UNPAID có dueDate < today
		List<Invoices> allInvoices = invoiceRepository.findAll();
		List<Invoices> toUpdate = new ArrayList<>();

		for (Invoices invoice : allInvoices) {
			if (invoice.getStatus() == InvoiceStatus.UNPAID && invoice.getDueDate().isBefore(today)) {
				invoice.setStatus(InvoiceStatus.OVERDUE);
				toUpdate.add(invoice);
			}
		}

		if (!toUpdate.isEmpty()) {
			invoiceRepository.saveAll(toUpdate);
		}

		return toUpdate.size();
	}

	@Override
	public List<Invoices> getInvoicesByMonthAndYear(int month, int year) {
		return invoiceRepository.findByMonthAndYear(month, year);
	}

	@Override
	public List<Invoices> findByStudentId(int studentId) {
		return invoiceRepository.findByStudentId(studentId);
	}

	@Override
	public Invoices findById(int id) {
		return invoiceRepository.findById(id).orElse(null);
	}

	@Override
	public BigDecimal calculateTotalUnpaidAmount(int studentId) {
		List<Invoices> unpaidInvoices = invoiceRepository.findByStudentIdAndStatus(studentId, InvoiceStatus.UNPAID);
		List<Invoices> overdueInvoices = invoiceRepository.findByStudentIdAndStatus(studentId, InvoiceStatus.OVERDUE);

		BigDecimal totalUnpaid = unpaidInvoices.stream().map(Invoices::getTotalAmount).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		BigDecimal totalOverdue = overdueInvoices.stream().map(Invoices::getTotalAmount).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		return totalUnpaid.add(totalOverdue);
	}

	@Override
	public Payments processPayment(int invoiceId, BigDecimal amount, Payments.PaymentMethod method,
			String transactionId) {
		Optional<Invoices> invoiceOpt = invoiceRepository.findById(invoiceId);
		if (invoiceOpt.isEmpty()) {
			throw new RuntimeException("Invoice not found with id: " + invoiceId);
		}

		Invoices invoice = invoiceOpt.get();

		// Kiểm tra trạng thái hóa đơn trước khi thanh toán
		if (invoice.getStatus() == InvoiceStatus.PAID) {
			throw new RuntimeException("Hóa đơn này đã được thanh toán trước đó!");
		}

		// Kiểm tra transactionId trùng lặp
		if (transactionId != null && !transactionId.trim().isEmpty()) {
			List<Payments> existingPayments = paymentRepository.findByInvoiceId(invoiceId);
			boolean duplicateTransaction = existingPayments.stream().anyMatch(
					p -> transactionId.equals(p.getTransactionId()) && p.getStatus() == Payments.PaymentStatus.SUCCESS);
			if (duplicateTransaction) {
				throw new RuntimeException("Mã giao dịch này đã được sử dụng!");
			}
		}

		Payments payment = new Payments();
		payment.setInvoice(invoice);
		payment.setAmount(amount);
		payment.setPaymentMethod(method);
		payment.setTransactionId(transactionId);
		payment.setPaymentDate(LocalDateTime.now());
		payment.setStatus(Payments.PaymentStatus.SUCCESS);

		Payments savedPayment = paymentRepository.save(payment);

		// Mark invoice as paid - Optimistic Locking sẽ throw exception nếu có xung đột
		markAsPaid(invoiceId);

		return savedPayment;
	}

	private String generateInvoiceNumber(int roomId, int month, int year) {
		return String.format("PSH%d%02d%03d%04d", year, month, roomId, System.currentTimeMillis() % 10000);
	}
}
