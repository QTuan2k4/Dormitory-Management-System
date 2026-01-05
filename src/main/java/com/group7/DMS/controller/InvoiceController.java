package com.group7.DMS.controller;

import com.group7.DMS.entity.Buildings;
import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Invoices.InvoiceStatus;
import com.group7.DMS.entity.Rooms;
import com.group7.DMS.repository.BuildingRepository;
import com.group7.DMS.repository.RoomRepository;
import com.group7.DMS.service.InvoiceService;
import com.group7.DMS.config.InvoicePricingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/invoices")
public class InvoiceController {

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private BuildingRepository buildingRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private InvoicePricingConfig pricingConfig;

	/**
	 * Trang chủ: Chọn tòa nhà GET /admin/invoices
	 */
	@GetMapping
	public String listInvoices(@RequestParam(required = false) Integer buildingId,
			@RequestParam(required = false) String roomNumber, @RequestParam(required = false) InvoiceStatus status,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Model model) {

		// Lấy danh sách tòa nhà
		List<Buildings> buildings = buildingRepository.findAll();
		model.addAttribute("buildings", buildings);

		// Tìm kiếm hóa đơn
		Page<Invoices> invoicesPage = invoiceService.searchInvoices(buildingId, roomNumber, status, month, year, page,
				size);

		// Thống kê
		Map<String, Object> summary = invoiceService.getInvoiceSummary();

		model.addAttribute("invoices", invoicesPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", invoicesPage.getTotalPages());
		model.addAttribute("totalItems", invoicesPage.getTotalElements());
		model.addAttribute("pageSize", size);
		model.addAttribute("buildingId", buildingId);
		model.addAttribute("roomNumber", roomNumber);
		model.addAttribute("status", status);
		model.addAttribute("month", month);
		model.addAttribute("year", year);
		model.addAttribute("summary", summary);

		return "admin/invoice/list";
	}

	/**
	 * Chọn tòa nhà và phòng để tạo hóa đơn GET /admin/invoices/select-room
	 */
	@GetMapping("/select-room")
	public String selectRoomPage(@RequestParam(required = false) Integer buildingId, Model model) {

		// Chỉ lấy tòa nhà không bảo trì
		List<Buildings> buildings = buildingRepository.findActiveBuildings();
		model.addAttribute("buildings", buildings);

		if (buildingId != null) {
			List<Rooms> rooms = roomRepository.findByBuildingId(buildingId);
			model.addAttribute("rooms", rooms);
			model.addAttribute("selectedBuildingId", buildingId);
		}

		return "admin/invoice/select-room";
	}

	/**
	 * Form tạo hóa đơn cho phòng đã chọn GET /admin/invoices/create
	 */
	@GetMapping("/create")
	public String showCreateForm(@RequestParam int roomId, Model model) {

		Rooms room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

		// Lấy tháng/năm hiện tại
		YearMonth currentMonth = YearMonth.now();

		model.addAttribute("room", room);
		model.addAttribute("currentMonth", currentMonth.getMonthValue());
		model.addAttribute("currentYear", currentMonth.getYear());

		model.addAttribute("electricityPrice", pricingConfig.getElectricityPricePerKwh());
		model.addAttribute("waterPrice", pricingConfig.getWaterPricePerM3());
		model.addAttribute("internetFee", pricingConfig.getInternetFeePerMonth());

		return "admin/invoice/form";
	}

	/**
	 * Xử lý tạo hóa đơn POST /admin/invoices/create
	 */
	@PostMapping("/create")
	public String createInvoice(@RequestParam int roomId, @RequestParam int month, @RequestParam int year,
			@RequestParam int electricityUsage, @RequestParam int waterUsage, RedirectAttributes redirectAttributes) {

		try {
			Invoices invoice = invoiceService.createInvoiceForRoom(roomId, month, year, electricityUsage, waterUsage);

			redirectAttributes.addFlashAttribute("message", "Tạo hóa đơn thành công: " + invoice.getInvoiceNumber());
			return "redirect:/admin/invoices/" + invoice.getId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/admin/invoices/create?roomId=" + roomId;
		}
	}

	/**
	 * Tạo hóa đơn hàng loạt cho tháng GET /admin/invoices/bulk-create
	 */
	@GetMapping("/bulk-create")
	public String showBulkCreateForm(Model model) {
		// Chỉ lấy tòa nhà không bảo trì
		List<Buildings> buildings = buildingRepository.findActiveBuildings();
		model.addAttribute("buildings", buildings);

		YearMonth currentMonth = YearMonth.now();
		model.addAttribute("currentMonth", currentMonth.getMonthValue());
		model.addAttribute("currentYear", currentMonth.getYear());

		return "admin/invoice/bulk-create";
	}

	/**
	 * Xử lý tạo hàng loạt POST /admin/invoices/bulk-create
	 */
	@PostMapping("/bulk-create")
	public String processBulkCreate(@RequestParam int buildingId, @RequestParam int month, @RequestParam int year,
			RedirectAttributes redirectAttributes) {

		try {
			// Lấy tất cả phòng trong tòa
			List<Rooms> rooms = roomRepository.findByBuildingId(buildingId);
			List<Integer> roomIds = rooms.stream().map(Rooms::getId).toList();

			Map<String, Object> result = invoiceService.createBulkInvoices(roomIds, month, year);

			redirectAttributes.addFlashAttribute("message", "Đã tạo " + result.get("successCount")
					+ " hóa đơn thành công. " + "Thất bại: " + result.get("failCount"));

			if (result.get("failCount") != null && (int) result.get("failCount") > 0) {
				redirectAttributes.addFlashAttribute("errorMessage", result.get("errors"));
			}

			return "redirect:/admin/invoices?buildingId=" + buildingId + "&month=" + month + "&year=" + year;
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/admin/invoices/bulk-create";
		}
	}

	/**
	 * Chi tiết hóa đơn GET /admin/invoices/{id}
	 */
	@GetMapping("/{id}")
	public String showInvoiceDetail(@PathVariable int id, Model model) {
		try {
			Invoices invoice = invoiceService.getInvoiceById(id);
			model.addAttribute("invoice", invoice);
			return "admin/invoice/detail";
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "redirect:/admin/invoices";
		}
	}

	/**
	 * Form chỉnh sửa GET /admin/invoices/{id}/edit
	 */
	@GetMapping("/{id}/edit")
	public String showEditForm(@PathVariable int id, Model model) {
		try {
			Invoices invoice = invoiceService.getInvoiceById(id);

			if (invoice.getStatus() == InvoiceStatus.PAID) {
				model.addAttribute("errorMessage", "Không thể sửa hóa đơn đã thanh toán");
				return "redirect:/admin/invoices/" + id;
			}

			model.addAttribute("invoice", invoice);

			model.addAttribute("electricityPrice", pricingConfig.getElectricityPricePerKwh());
			model.addAttribute("waterPrice", pricingConfig.getWaterPricePerM3());
			model.addAttribute("internetFee", pricingConfig.getInternetFeePerMonth());

			return "admin/invoice/edit";
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "redirect:/admin/invoices";
		}
	}

	/**
	 * Xử lý cập nhật POST /admin/invoices/{id}/edit
	 */
	@PostMapping("/{id}/edit")
	public String updateInvoice(@PathVariable int id, @RequestParam Integer electricityUsage,
			@RequestParam Integer waterUsage, RedirectAttributes redirectAttributes) {

		try {
			invoiceService.updateInvoice(id, electricityUsage, waterUsage);
			redirectAttributes.addFlashAttribute("success", "Cập nhật thành công");
			return "redirect:/admin/invoices/" + id;
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/admin/invoices/" + id + "/edit";
		}
	}

	/**
	 * Thanh toán POST /admin/invoices/{id}/pay
	 */
	@PostMapping("/{id}/pay")
	public String markAsPaid(@PathVariable int id, RedirectAttributes redirectAttributes) {
		try {
			invoiceService.markAsPaid(id);
			redirectAttributes.addFlashAttribute("success", "Đã đánh dấu thanh toán");
			return "redirect:/admin/invoices/" + id;
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/admin/invoices/" + id;
		}
	}

	/**
	 * Xóa hóa đơn POST /admin/invoices/{id}/delete
	 */
	@PostMapping("/{id}/delete")
	public String deleteInvoice(@PathVariable int id, RedirectAttributes redirectAttributes) {
		try {
			invoiceService.deleteInvoice(id);
			redirectAttributes.addFlashAttribute("message", "Xóa hóa đơn thành công");
			return "redirect:/admin/invoices";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/admin/invoices/" + id;
		}
	}

	/**
	 * In hóa đơn GET /admin/invoices/{id}/print
	 */
	@GetMapping("/{id}/print")
	public String printInvoice(@PathVariable int id, Model model) {
		Invoices invoice = invoiceService.getInvoiceById(id);
		model.addAttribute("invoice", invoice);
		return "admin/invoice/print";
	}
}