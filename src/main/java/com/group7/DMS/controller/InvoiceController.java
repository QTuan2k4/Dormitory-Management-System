package com.group7.DMS.controller;

import com.group7.DMS.entity.Contracts;
import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;
import com.group7.DMS.service.ContractService;
import com.group7.DMS.service.InvoiceService;
import com.group7.DMS.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/invoices")
public class InvoiceController {

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private ContractService contractService;

	@Autowired
	private NotificationService notificationService;

	@GetMapping({ "", "/" })
	public String listInvoices(@RequestParam(required = false) String search,
			@RequestParam(required = false) String status, @RequestParam(required = false) String monthYear,
			@RequestParam(required = false) Integer paramYear,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, Model model,
			Authentication auth) {

		// Tính toán ngày bắt đầu và kết thúc từ tháng/năm
		LocalDate now = LocalDate.now();
		int month = now.getMonthValue();
		int year = now.getYear(); // Biến year không bị trùng nữa

		// Nếu monthYear có giá trị, tách month và year
		if (monthYear != null && !monthYear.isEmpty()) {
			String[] parts = monthYear.split("-");
			if (parts.length == 2) {
				year = Integer.parseInt(parts[0]); // Gán lại year từ monthYear
				month = Integer.parseInt(parts[1]); // Gán lại month từ monthYear
			}
		} else if (paramYear != null) {
			year = paramYear; // Nếu paramYear có giá trị, sử dụng giá trị đó
		}

		// Tính toán phạm vi ngày từ tháng/năm
		LocalDate[] dateRange = calculateDateRange(month, year);
		final LocalDate finalFromDate = dateRange[0];
		final LocalDate finalToDate = dateRange[1];

		// Chuyển đổi status từ String thành InvoiceStatus (enum)
		Invoices.InvoiceStatus invoiceStatus = null;
		if (status != null && !status.isEmpty()) {
			try {
				invoiceStatus = Invoices.InvoiceStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				invoiceStatus = null; // Nếu giá trị không hợp lệ, có thể bỏ qua bộ lọc status
			}
		}

		// Phân trang
		Pageable pageable = PageRequest.of(page - 1, size);

		// Lọc và phân trang hóa đơn
		Page<Invoices> invoicePage = invoiceService.findInvoicesWithFilters(search, invoiceStatus, finalFromDate,
				finalToDate, pageable);

		// Lấy danh sách hóa đơn đã phân trang
		List<Invoices> invoices = invoicePage.getContent();

		// Tổng số hóa đơn
		int totalInvoices = (int) invoicePage.getTotalElements();
		int totalPages = invoicePage.getTotalPages();

		// Tổng tiền của hóa đơn
		BigDecimal totalAmount = invoices.stream().map(Invoices::getTotalAmount).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		BigDecimal paidAmount = invoices.stream().filter(inv -> inv.getStatus() == Invoices.InvoiceStatus.PAID)
				.map(Invoices::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal unpaidAmount = totalAmount.subtract(paidAmount);

		// Truyền thông tin vào view
		model.addAttribute("invoices", invoices);
		model.addAttribute("search", search);
		model.addAttribute("status", status);
		model.addAttribute("month", month);
		model.addAttribute("year", year); // Truyền year và month vào view
		model.addAttribute("fromDate", finalFromDate);
		model.addAttribute("toDate", finalToDate);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("pageSize", size);
		model.addAttribute("totalInvoices", totalInvoices);
		model.addAttribute("totalAmount", totalAmount);
		model.addAttribute("paidAmount", paidAmount);
		model.addAttribute("unpaidAmount", unpaidAmount);

		if (auth != null) {
			model.addAttribute("username", auth.getName());
		}

		return "admin/invoice/list";
	}

	// Hàm tính ngày bắt đầu và kết thúc cho tháng/năm
	private LocalDate[] calculateDateRange(Integer month, Integer year) {
		LocalDate now = LocalDate.now();
		if (month == null && year == null) {
			return new LocalDate[] { now.withDayOfMonth(1), now };
		}
		if (month != null && year != null) {
			LocalDate fromDate = LocalDate.of(year, month, 1);
			LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
			return new LocalDate[] { fromDate, toDate };
		}
		return new LocalDate[] { null, null };
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		List<Contracts> activeContracts = contractService.findByStatus(Contracts.ContractStatus.ACTIVE);
		model.addAttribute("contracts", activeContracts);
		model.addAttribute("invoice", new Invoices());
		return "admin/invoice/form";
	}

	@PostMapping("/create")
	public String createInvoice(@RequestParam int contractId, @RequestParam String invoiceNumber,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
			@RequestParam BigDecimal roomFee,
			@RequestParam(required = false, defaultValue = "0") BigDecimal electricityFee,
			@RequestParam(required = false, defaultValue = "0") BigDecimal waterFee,
			@RequestParam(required = false, defaultValue = "0") BigDecimal internetFee, RedirectAttributes ra) {

		try {
			if (invoiceService.findByInvoiceNumber(invoiceNumber) != null) {
				ra.addFlashAttribute("error", "Mã hóa đơn đã tồn tại!");
				return "redirect:/admin/invoices/create";
			}

			Invoices invoice = invoiceService.createInvoice(contractId, invoiceNumber, issueDate, dueDate, roomFee,
					electricityFee, waterFee, internetFee);

			Contracts contract = contractService.findById(contractId);
			if (contract != null) {
				notificationService.createNotification(contract.getStudent().getUser(), "Hóa đơn mới",
						String.format("Bạn có hóa đơn mới số %s với tổng số tiền %s VNĐ. Hạn thanh toán: %s",
								invoiceNumber, invoice.getTotalAmount(), dueDate),
						com.group7.DMS.entity.Notifications.NotificationType.REMINDER_PAYMENT,
						com.group7.DMS.entity.Notifications.SentVia.BOTH);
			}

			ra.addFlashAttribute("success", "Tạo hóa đơn thành công!");
			return "redirect:/admin/invoices/" + invoice.getId();

		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
			return "redirect:/admin/invoices/create";
		}
	}

	@GetMapping("/{id}")
	public String viewInvoiceDetail(@PathVariable int id, Model model) {
		Invoices invoice = invoiceService.findById(id);
		if (invoice == null) {
			return "redirect:/admin/invoices";
		}
		model.addAttribute("invoice", invoice);
		return "admin/invoice/detail";
	}

	@GetMapping("/{id}/edit")
	public String showEditForm(@PathVariable int id, Model model, RedirectAttributes ra) {
		Invoices invoice = invoiceService.findById(id);
		if (invoice == null) {
			ra.addFlashAttribute("error", "Không tìm thấy hóa đơn!");
			return "redirect:/admin/invoices";
		}

		if (invoice.getStatus() == Invoices.InvoiceStatus.PAID) {
			ra.addFlashAttribute("error", "Không thể chỉnh sửa hóa đơn đã thanh toán!");
			return "redirect:/admin/invoices/" + id;
		}

		model.addAttribute("invoice", invoice);
		return "admin/invoice/edit";
	}

	@PostMapping("/{id}/edit")
	public String updateInvoice(@PathVariable int id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
			@RequestParam BigDecimal roomFee,
			@RequestParam(required = false, defaultValue = "0") BigDecimal electricityFee,
			@RequestParam(required = false, defaultValue = "0") BigDecimal waterFee,
			@RequestParam(required = false, defaultValue = "0") BigDecimal internetFee, RedirectAttributes ra) {

		try {
			Invoices invoice = invoiceService.findById(id);
			if (invoice == null) {
				ra.addFlashAttribute("error", "Không tìm thấy hóa đơn!");
				return "redirect:/admin/invoices";
			}

			if (invoice.getStatus() == Invoices.InvoiceStatus.PAID) {
				ra.addFlashAttribute("error", "Không thể chỉnh sửa hóa đơn đã thanh toán!");
				return "redirect:/admin/invoices/" + id;
			}

			invoice.setIssueDate(issueDate);
			invoice.setDueDate(dueDate);
			invoice.setRoomFee(roomFee);
			invoice.setElectricityFee(electricityFee);
			invoice.setWaterFee(waterFee);
			invoice.setInternetFee(internetFee);

			invoiceService.update(invoice);

			ra.addFlashAttribute("success", "Cập nhật hóa đơn thành công!");
			return "redirect:/admin/invoices/" + id;

		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
			return "redirect:/admin/invoices/" + id + "/edit";
		}
	}

	@GetMapping("/{id}/payment")
	public String showPaymentForm(@PathVariable int id, Model model, RedirectAttributes ra) {
		Invoices invoice = invoiceService.findById(id);
		if (invoice == null) {
			ra.addFlashAttribute("error", "Không tìm thấy hóa đơn!");
			return "redirect:/admin/invoices";
		}

		if (invoice.getStatus() == Invoices.InvoiceStatus.PAID) {
			ra.addFlashAttribute("error", "Hóa đơn này đã được thanh toán!");
			return "redirect:/admin/invoices/" + id;
		}

		model.addAttribute("invoice", invoice);
		return "admin/invoice/payment";
	}

	@PostMapping("/{id}/payment")
	public String processPayment(@PathVariable int id, @RequestParam String paymentMethod,
			@RequestParam String transactionId, RedirectAttributes ra) {

		try {
			Invoices invoice = invoiceService.findById(id);
			if (invoice == null) {
				ra.addFlashAttribute("error", "Không tìm thấy hóa đơn!");
				return "redirect:/admin/invoices";
			}

			if (invoice.getStatus() == Invoices.InvoiceStatus.PAID) {
				ra.addFlashAttribute("error", "Hóa đơn này đã được thanh toán!");
				return "redirect:/admin/invoices/" + id;
			}

			Payments.PaymentMethod method = Payments.PaymentMethod.valueOf(paymentMethod.toUpperCase());
			invoiceService.processPayment(id, invoice.getTotalAmount(), method, transactionId);

			notificationService.createNotification(invoice.getContract().getStudent().getUser(),
					"Thanh toán thành công",
					String.format("Hóa đơn số %s đã được thanh toán thành công. Số tiền: %s VNĐ. Phương thức: %s",
							invoice.getInvoiceNumber(), invoice.getTotalAmount(), method),
					com.group7.DMS.entity.Notifications.NotificationType.REMINDER_PAYMENT,
					com.group7.DMS.entity.Notifications.SentVia.BOTH);

			ra.addFlashAttribute("success", "Thanh toán thành công!");
			return "redirect:/admin/invoices/" + id;

		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
			return "redirect:/admin/invoices/" + id + "/payment";
		}
	}

	@PostMapping("/{id}/delete")
	public String deleteInvoice(@PathVariable int id, RedirectAttributes ra) {
		try {
			Invoices invoice = invoiceService.findById(id);
			if (invoice == null) {
				ra.addFlashAttribute("error", "Không tìm thấy hóa đơn!");
				return "redirect:/admin/invoices";
			}

			if (invoice.getStatus() == Invoices.InvoiceStatus.PAID) {
				ra.addFlashAttribute("error", "Không thể xóa hóa đơn đã thanh toán!");
				return "redirect:/admin/invoices/" + id;
			}

			invoiceService.delete(id);
			ra.addFlashAttribute("success", "Xóa hóa đơn thành công!");
			return "redirect:/admin/invoices";

		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
			return "redirect:/admin/invoices";
		}
	}

	@GetMapping("/reports")
	public String showFinancialReports(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) String reportType, Model model) {

		if (startDate == null) {
			startDate = LocalDate.now().withDayOfMonth(1);
		}
		if (endDate == null) {
			endDate = LocalDate.now();
		}

		List<Invoices> invoices = invoiceService.findByDateRange(startDate, endDate);

		BigDecimal totalRevenue = invoices.stream().filter(inv -> inv.getStatus() == Invoices.InvoiceStatus.PAID)
				.map(Invoices::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalUnpaid = invoices.stream()
				.filter(inv -> inv.getStatus() == Invoices.InvoiceStatus.UNPAID
						|| inv.getStatus() == Invoices.InvoiceStatus.OVERDUE)
				.map(Invoices::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		long totalInvoices = invoices.size();
		long paidInvoices = invoices.stream().filter(inv -> inv.getStatus() == Invoices.InvoiceStatus.PAID).count();
		long unpaidInvoices = totalInvoices - paidInvoices;

		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("reportType", reportType);
		model.addAttribute("invoices", invoices);
		model.addAttribute("totalRevenue", totalRevenue);
		model.addAttribute("totalUnpaid", totalUnpaid);
		model.addAttribute("totalInvoices", totalInvoices);
		model.addAttribute("paidInvoices", paidInvoices);
		model.addAttribute("unpaidInvoices", unpaidInvoices);

		return "admin/invoice/reports";
	}

	@GetMapping("/{id}/print")
	public String printInvoice(@PathVariable int id, Model model) {
		Invoices invoice = invoiceService.findById(id);
		if (invoice == null) {
			return "redirect:/admin/invoices";
		}
		model.addAttribute("invoice", invoice);
		return "admin/invoice/print";
	}
}
