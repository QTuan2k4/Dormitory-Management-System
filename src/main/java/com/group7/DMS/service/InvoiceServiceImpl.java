package com.group7.DMS.service;

import com.group7.DMS.config.InvoicePricingConfig;
import com.group7.DMS.entity.Contracts;
import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Invoices.InvoiceStatus;
import com.group7.DMS.entity.Rooms;
import com.group7.DMS.repository.ContractRepository;
import com.group7.DMS.repository.InvoiceRepository;
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
import java.time.YearMonth;
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
	private InvoicePricingConfig pricingConfig;

	@Override
	public Invoices createInvoiceForRoom(int roomId, int month, int year, int electricityUsage, int waterUsage) {

		// Validate input
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

		// Kiểm tra phòng tồn tại
		Rooms room = roomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Phòng không tồn tại với ID: " + roomId));

		// Kiểm tra đã có hóa đơn cho tháng này chưa
		Optional<Invoices> existing = invoiceRepository.findByRoomIdAndMonthAndYear(roomId, month, year);
		if (existing.isPresent()) {
			throw new RuntimeException(
					"Đã tồn tại hóa đơn cho phòng " + room.getRoomNumber() + " tháng " + month + "/" + year);
		}

		// Tạo hóa đơn mới
		Invoices invoice = new Invoices();

		// Set room (required)
		invoice.setRoom(room);

		// Lấy hợp đồng active đầu tiên của phòng (optional)
		List<Contracts> activeContracts = contractRepository.findActiveContractsByRoom(roomId);
		if (!activeContracts.isEmpty()) {
			invoice.setContract(activeContracts.get(0));
		}

		// Tạo mã hóa đơn tự động
		String invoiceNumber = generateInvoiceNumber(roomId, month, year);
		invoice.setInvoiceNumber(invoiceNumber);

		// Set month và year (QUAN TRỌNG!)
		invoice.setMonth(month);
		invoice.setYear(year);

		// Set dates
		LocalDate issueDate = LocalDate.of(year, month, 1);
		invoice.setIssueDate(issueDate);

		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate dueDate = yearMonth.atEndOfMonth();
		invoice.setDueDate(dueDate);

		// Set usage
		invoice.setElectricityUsage(electricityUsage);
		invoice.setWaterUsage(waterUsage);

		// Set tất cả các trường số với giá trị mặc định (KHÔNG NULL)
		invoice.setRoomFee(BigDecimal.ZERO);

		// Tính tiền điện (đảm bảo không null)
		BigDecimal electricityFee = pricingConfig.getElectricityPricePerKwh()
				.multiply(new BigDecimal(electricityUsage));
		invoice.setElectricityFee(electricityFee != null ? electricityFee : BigDecimal.ZERO);

		// Tính tiền nước (đảm bảo không null)
		BigDecimal waterFee = pricingConfig.getWaterPricePerM3().multiply(new BigDecimal(waterUsage));
		invoice.setWaterFee(waterFee != null ? waterFee : BigDecimal.ZERO);

		// Phí Internet cố định (đảm bảo không null)
		BigDecimal internetFee = pricingConfig.getInternetFeePerMonth();
		invoice.setInternetFee(internetFee != null ? internetFee : BigDecimal.ZERO);

		// Set status (required)
		invoice.setStatus(InvoiceStatus.UNPAID);

		return invoiceRepository.save(invoice);
	}

	@Override
	public Invoices createInvoiceForContract(int contractId, String invoiceNumber, LocalDate issueDate,
			LocalDate dueDate, int electricityUsage, int waterUsage) {

		// Kiểm tra contract tồn tại
		Contracts contract = contractRepository.findById(contractId)
				.orElseThrow(() -> new RuntimeException("Contract không tồn tại với ID: " + contractId));

		// Kiểm tra contract phải đang ACTIVE
		if (contract.getStatus() != Contracts.ContractStatus.ACTIVE) {
			throw new RuntimeException("Contract không ở trạng thái ACTIVE");
		}

		// Kiểm tra số hóa đơn đã tồn tại chưa
		if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
			throw new RuntimeException("Số hóa đơn đã tồn tại: " + invoiceNumber);
		}

		// Validate ngày
		if (dueDate.isBefore(issueDate)) {
			throw new RuntimeException("Ngày đến hạn phải sau ngày phát hành");
		}

		// Tạo invoice mới
		Invoices invoice = new Invoices();
		invoice.setContract(contract);
		invoice.setRoom(contract.getRoom());
		invoice.setInvoiceNumber(invoiceNumber);
		invoice.setIssueDate(issueDate);
		invoice.setDueDate(dueDate);
		invoice.setStatus(InvoiceStatus.UNPAID);

		// Phí phòng = 0 (chỉ tính phí sinh hoạt)
		invoice.setRoomFee(BigDecimal.ZERO);

		// Tính tiền điện
		BigDecimal electricityFee = pricingConfig.getElectricityPricePerKwh()
				.multiply(new BigDecimal(electricityUsage));
		invoice.setElectricityFee(electricityFee);

		// Tính tiền nước
		BigDecimal waterFee = pricingConfig.getWaterPricePerM3().multiply(new BigDecimal(waterUsage));
		invoice.setWaterFee(waterFee);

		// Phí Internet cố định
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
				// Mặc định số điện = 0, số nước = 0 (admin sẽ cập nhật sau)
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

		// Cập nhật số điện
		if (electricityUsage != null) {
			BigDecimal electricityFee = pricingConfig.getElectricityPricePerKwh()
					.multiply(new BigDecimal(electricityUsage));
			invoice.setElectricityFee(electricityFee);
		}

		// Cập nhật số nước
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

		BigDecimal totalPaidAmount = invoiceRepository.sumTotalAmountByStatus(InvoiceStatus.PAID);
		BigDecimal totalUnpaidAmount = invoiceRepository.sumTotalAmountByStatus(InvoiceStatus.UNPAID);
		BigDecimal totalOverdueAmount = invoiceRepository.sumTotalAmountByStatus(InvoiceStatus.OVERDUE);

		long countPaid = invoiceRepository.countByStatus(InvoiceStatus.PAID);
		long countUnpaid = invoiceRepository.countByStatus(InvoiceStatus.UNPAID);
		long countOverdue = invoiceRepository.countByStatus(InvoiceStatus.OVERDUE);

		summary.put("totalPaidAmount", totalPaidAmount);
		summary.put("totalUnpaidAmount", totalUnpaidAmount);
		summary.put("totalOverdueAmount", totalOverdueAmount);
		summary.put("countPaid", countPaid);
		summary.put("countUnpaid", countUnpaid);
		summary.put("countOverdue", countOverdue);
		summary.put("totalInvoices", countPaid + countUnpaid + countOverdue);

		return summary;
	}

	@Override
	public int updateOverdueInvoices() {
		List<Invoices> overdueInvoices = invoiceRepository.findOverdueInvoices(LocalDate.now());

		for (Invoices invoice : overdueInvoices) {
			invoice.setStatus(InvoiceStatus.OVERDUE);
		}

		invoiceRepository.saveAll(overdueInvoices);
		return overdueInvoices.size();
	}

	@Override
	public List<Invoices> getInvoicesByMonthAndYear(int month, int year) {
		return invoiceRepository.findByMonthAndYear(month, year);
	}

	/**
	 * Tạo mã hóa đơn tự động Format: PSH + năm + tháng + roomId + timestamp
	 */
	private String generateInvoiceNumber(int roomId, int month, int year) {
		return String.format("PSH%d%02d%03d%04d", year, month, roomId, System.currentTimeMillis() % 10000);
	}
}