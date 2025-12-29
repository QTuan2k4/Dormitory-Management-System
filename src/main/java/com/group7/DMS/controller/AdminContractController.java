package com.group7.DMS.controller;

import com.group7.DMS.entity.Buildings;
import com.group7.DMS.entity.Contracts;
import com.group7.DMS.entity.Rooms;
import com.group7.DMS.service.BuildingService;
import com.group7.DMS.service.ContractService;
import com.group7.DMS.service.RoomService;
import com.group7.DMS.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/contracts")
public class AdminContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private RoomService roomService;

    /**
     * GET /admin/contracts
     * Danh sách hợp đồng + lọc + PHÂN TRANG
     */
    @GetMapping
    public String listContracts(
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) Integer roomId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        // 1) dữ liệu gốc
        List<Contracts> contracts = contractService.findAll();

        // 2) lọc theo student
        if (studentId != null) {
            contracts = contracts.stream()
                    .filter(c -> c.getStudent() != null && c.getStudent().getId() == studentId)
                    .toList();
        }

        // 3) lọc theo room
        if (roomId != null) {
            contracts = contracts.stream()
                    .filter(c -> c.getRoom() != null && c.getRoom().getId() == roomId)
                    .toList();
        }

        // 4) lọc theo status
        if (status != null && !status.isBlank()) {
            try {
                Contracts.ContractStatus st = Contracts.ContractStatus.valueOf(status.trim().toUpperCase());
                contracts = contracts.stream()
                        .filter(c -> c.getStatus() == st)
                        .toList();
            } catch (IllegalArgumentException ignored) {
                // status không hợp lệ thì bỏ qua
            }
        }

        // 5) sort newest first (startDate desc, null last)
        contracts = contracts.stream()
                .sorted((a, b) -> {
                    LocalDateTime sa = a.getStartDate();
                    LocalDateTime sb = b.getStartDate();
                    if (sa == null && sb == null) return 0;
                    if (sa == null) return 1;
                    if (sb == null) return -1;
                    return sb.compareTo(sa);
                })
                .toList();

        // 6) phân trang giống student-list
        int pageSize = size > 0 ? size : 10;
        int totalItems = contracts.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;

        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<Contracts> pagedContracts = contracts.subList(fromIndex, toIndex);

        // 7) model
        model.addAttribute("contracts", pagedContracts);
        model.addAttribute("statuses", Contracts.ContractStatus.values());

        model.addAttribute("studentId", studentId);
        model.addAttribute("roomId", roomId);
        model.addAttribute("status", status);

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalItems", totalItems);

        // dropdown filter
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("rooms", getAllRoomsForDropdown());

        model.addAttribute("pageTitle", "Quản lý hợp đồng");
        return "admin/contract/list";
    }

    /**
     * GET /admin/contracts/{id}
     * Chi tiết hợp đồng
     * View: admin/contract/detail
     */
    @GetMapping("/{id}")
    public String contractDetail(@PathVariable int id, Model model, RedirectAttributes ra) {
        Contracts contract = contractService.findById(id);
        if (contract == null) {
            ra.addFlashAttribute("error", "Không tìm thấy hợp đồng #" + id);
            return "redirect:/admin/contracts";
        }

        model.addAttribute("contract", contract);
        model.addAttribute("statuses", Contracts.ContractStatus.values());
        model.addAttribute("pageTitle", "Hợp đồng #" + id);
        return "admin/contract/detail";
    }

    /**
     * GET /admin/contracts/edit/{id}
     * Form sửa hợp đồng
     * View: admin/contract/form
     */
    @GetMapping("/edit/{id}")
    public String editContractForm(@PathVariable int id, Model model, RedirectAttributes ra) {
        Contracts contract = contractService.findById(id);
        if (contract == null) {
            ra.addFlashAttribute("error", "Không tìm thấy hợp đồng #" + id);
            return "redirect:/admin/contracts";
        }

        model.addAttribute("contract", contract);
        model.addAttribute("statuses", Contracts.ContractStatus.values());
        model.addAttribute("pageTitle", "Sửa hợp đồng #" + id);
        return "admin/contract/form";
    }

    /**
     * POST /admin/contracts/update/{id}
     * Update hợp đồng (start/end/manualFee/status)
     */
    @PostMapping("/update/{id}")
    public String updateContract(
            @PathVariable int id,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) BigDecimal manualFee,
            @RequestParam(required = false) String status,
            RedirectAttributes ra
    ) {
        Contracts contract = contractService.findById(id);
        if (contract == null) {
            ra.addFlashAttribute("error", "Không tìm thấy hợp đồng #" + id);
            return "redirect:/admin/contracts";
        }

        try {
            LocalDateTime sd = parseDateTimeFlexible(startDate);
            LocalDateTime ed = parseDateTimeFlexible(endDate);

            if (sd != null) contract.setStartDate(sd);
            if (ed != null) contract.setEndDate(ed);

            if (contract.getStartDate() != null && contract.getEndDate() != null
                    && contract.getEndDate().isBefore(contract.getStartDate())) {
                ra.addFlashAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu.");
                return "redirect:/admin/contracts/edit/" + id;
            }

            if (manualFee != null) contract.setManualFee(manualFee);
            if (contract.getManualFee() == null) contract.setManualFee(BigDecimal.ZERO); // manual_fee nullable=false

            if (status != null && !status.isBlank()) {
                Contracts.ContractStatus newStatus = Contracts.ContractStatus.valueOf(status.trim().toUpperCase());

                // Rule đơn giản: mỗi SV chỉ 1 ACTIVE
                if (newStatus == Contracts.ContractStatus.ACTIVE && contract.getStudent() != null) {
                    List<Contracts> activeOfStudent = contractService.findActiveContractsByStudent(contract.getStudent().getId());
                    boolean hasOtherActive = activeOfStudent.stream().anyMatch(c -> c.getId() != contract.getId());
                    if (hasOtherActive) {
                        ra.addFlashAttribute("error",
                                "Sinh viên này đã có hợp đồng ACTIVE khác. Hãy INACTIVE/TERMINATED hợp đồng cũ trước.");
                        return "redirect:/admin/contracts/edit/" + id;
                    }
                }

                contract.setStatus(newStatus);
            }

            contractService.update(contract);
            ra.addFlashAttribute("success", "Cập nhật hợp đồng thành công!");
            return "redirect:/admin/contracts/" + id;

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
            return "redirect:/admin/contracts/edit/" + id;
        }
    }

    /**
     * POST /admin/contracts/{id}/status
     * Đổi trạng thái nhanh
     */
    @PostMapping("/{id}/status")
    public String changeContractStatus(
            @PathVariable int id,
            @RequestParam("status") String status,
            RedirectAttributes ra
    ) {
        Contracts contract = contractService.findById(id);
        if (contract == null) {
            ra.addFlashAttribute("error", "Không tìm thấy hợp đồng #" + id);
            return "redirect:/admin/contracts";
        }

        try {
            Contracts.ContractStatus newStatus = Contracts.ContractStatus.valueOf(status.trim().toUpperCase());

            if (newStatus == Contracts.ContractStatus.ACTIVE && contract.getStudent() != null) {
                List<Contracts> activeOfStudent = contractService.findActiveContractsByStudent(contract.getStudent().getId());
                boolean hasOtherActive = activeOfStudent.stream().anyMatch(c -> c.getId() != contract.getId());
                if (hasOtherActive) {
                    ra.addFlashAttribute("error",
                            "Sinh viên này đã có hợp đồng ACTIVE khác. Hãy INACTIVE/TERMINATED hợp đồng cũ trước.");
                    return "redirect:/admin/contracts/" + id;
                }
            }

            contract.setStatus(newStatus);
            if (contract.getManualFee() == null) contract.setManualFee(BigDecimal.ZERO);

            contractService.update(contract);
            ra.addFlashAttribute("success", "Đã đổi trạng thái hợp đồng thành " + newStatus);
            return "redirect:/admin/contracts/" + id;

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi đổi trạng thái: " + e.getMessage());
            return "redirect:/admin/contracts/" + id;
        }
    }

    /**
     * GET /admin/contracts/expired
     * Danh sách hợp đồng hết hạn (endDate < now && status ACTIVE)
     * View: admin/contract/expired
     */
    @GetMapping("/expired")
    public String expiredContracts(Model model) {
        List<Contracts> expired = contractService.findExpiredContracts();
        model.addAttribute("contracts", expired);
        model.addAttribute("pageTitle", "Hợp đồng hết hạn");
        return "admin/contract/expired";
    }

    /**
     * POST /admin/contracts/expired/terminate
     * Terminate toàn bộ hợp đồng hết hạn (ACTIVE -> TERMINATED)
     */
    @PostMapping("/expired/terminate")
    public String terminateExpiredContracts(RedirectAttributes ra) {
        List<Contracts> expired = contractService.findExpiredContracts();
        int count = 0;

        for (Contracts c : expired) {
            c.setStatus(Contracts.ContractStatus.TERMINATED);
            if (c.getManualFee() == null) c.setManualFee(BigDecimal.ZERO);
            contractService.update(c);
            count++;
        }

        ra.addFlashAttribute("success", "Đã TERMINATED " + count + " hợp đồng hết hạn.");
        return "redirect:/admin/contracts/expired";
    }

    /**
     * (Tuỳ chọn) Xoá hợp đồng
     * POST /admin/contracts/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String deleteContract(@PathVariable int id, RedirectAttributes ra) {
        try {
            contractService.delete(id);
            ra.addFlashAttribute("success", "Đã xoá hợp đồng #" + id);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xoá: " + e.getMessage());
        }
        return "redirect:/admin/contracts";
    }

    // ===================== Helpers =====================

    private List<Rooms> getAllRoomsForDropdown() {
        List<Rooms> all = new ArrayList<>();
        List<Buildings> buildings = buildingService.getAllBuildings();
        for (Buildings b : buildings) {
            List<Rooms> rooms = roomService.findRoomsByBuildingId(b.getId());
            if (rooms != null) all.addAll(rooms);
        }
        return all;
    }

    /**
     * Parse linh hoạt:
     * - 2025-12-28T10:30:00
     * - 2025-12-28T10:30
     * - 2025-12-28 10:30
     * - 2025-12-28
     */
    private LocalDateTime parseDateTimeFlexible(String input) {
        if (input == null || input.isBlank()) return null;
        String s = input.trim();

        try { return LocalDateTime.parse(s); } catch (Exception ignored) {}
        try { return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")); } catch (Exception ignored) {}
        try { return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")); } catch (Exception ignored) {}
        try { return LocalDate.parse(s, DateTimeFormatter.ISO_DATE).atStartOfDay(); } catch (Exception ignored) {}

        throw new IllegalArgumentException("Định dạng ngày/giờ không hợp lệ: " + s);
    }
}
