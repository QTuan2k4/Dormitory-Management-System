package com.group7.DMS.controller;

import com.group7.DMS.entity.Buildings;
import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Rooms;
import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.repository.ContractRepository;
import com.group7.DMS.service.BuildingService;
import com.group7.DMS.service.InvoiceService;
import com.group7.DMS.service.RoomService;
import com.group7.DMS.service.StudentService;
import com.group7.DMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private StudentService studentService;
    @Autowired
    private BuildingService buildingService;

    @Autowired
    private RoomService roomService;
    
    @Autowired
    private ContractRepository contractRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());
        } else {
            // Fallback for in-memory users or users not present in DB
            model.addAttribute("username", username);
        }
        return "admin/dashboard";
    }

    @GetMapping("/invoices")
    public String invoiceList(Model model) {
        List<Invoices> invoices = invoiceService.findAll();
        model.addAttribute("invoices", invoices);
        return "admin/invoice-list";
    }

    @GetMapping("/invoices/{id}/pay")
    public String payInvoice(@PathVariable int id, Model model) {
        Invoices invoice = invoiceService.findById(id);
        if (invoice != null) {
            model.addAttribute("invoice", invoice);
            return "admin/payment";
        }
        return "redirect:/admin/invoices";
    }

    @PostMapping("/invoices/{id}/pay")
    public String processPayment(@PathVariable int id, 
                                @RequestParam String paymentMethod,
                                @RequestParam String transactionId) {
        Invoices invoice = invoiceService.findById(id);
        if (invoice != null) {
            Payments.PaymentMethod method = Payments.PaymentMethod.valueOf(paymentMethod.toUpperCase());
            invoiceService.processPayment(id, invoice.getTotalAmount(), method, transactionId);
        }
        return "redirect:/admin/invoices";
    }

    @GetMapping("/export-excel")
    public String exportExcel() {
        // TODO: Implement Excel export
        return "redirect:/admin/invoices";
    }

    @GetMapping("/import-excel")
    public String importExcel() {
        // TODO: Implement Excel import
        return "redirect:/admin/invoices";
    }

    // ========== Student Profile Functions ==========
    @GetMapping("/students")
    public String listStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model,
            Authentication auth) {

        // 1. Lấy tất cả sinh viên làm dữ liệu gốc
        List<Students> students = studentService.findAll();

        // 2. LỌC THEO TRẠNG THÁI (nếu có chọn)
        if (status != null && !status.isBlank()) {
            try {
                Students.RegistrationStatus regStatus = Students.RegistrationStatus.valueOf(status.toUpperCase());
                students = students.stream()
                        .filter(s -> s.getRegistrationStatus() == regStatus)
                        .toList();
            } catch (IllegalArgumentException e) {
                // status không hợp lệ → bỏ qua
            }
        }

        // 3. LỌC THEO TỪ KHÓA TÌM KIẾM (nếu có nhập)
        if (search != null && !search.isBlank()) {
            String keyword = search.trim().toLowerCase();
            students = students.stream()
                    .filter(s -> {
                        String fullName = (s.getFullName() != null) ? s.getFullName().toLowerCase() : "";
                        String studentId = (s.getStudentId() != null) ? s.getStudentId().toLowerCase() : "";
                        String phone = (s.getPhone() != null) ? s.getPhone() : "";
                        return fullName.contains(keyword) ||
                               studentId.contains(keyword) ||
                               phone.contains(keyword);
                    })
                    .toList();
        }

        // 4. Gửi dữ liệu ra view
        model.addAttribute("students", students);
        model.addAttribute("search", search);        // giữ từ khóa tìm kiếm
        model.addAttribute("status", status);        // giữ trạng thái đã chọn

        // Username cho header
        if (auth != null) {
            model.addAttribute("username", auth.getName());
        }

        return "admin/student-list";
    }

    // Alias mapping for legacy URL '/admin/student-list' -> redirect to '/admin/students'
    @GetMapping("/student-list")
    public String studentListAlias() {
        return "redirect:/admin/students";
    }

    @GetMapping("/students/{id}")
    public String studentDetail(@PathVariable int id, Model model) {
        Students student = studentService.findById(id);
        if (student != null) {
            // Parse documents path to map for image display
            if (student.getDocumentsPath() != null && !student.getDocumentsPath().isEmpty()) {
                model.addAttribute("documentMap", parseDocumentsPath(student.getDocumentsPath()));
            }
            model.addAttribute("student", student);
            return "admin/student-detail";
        }
        return "redirect:/admin/students";
    }
    
    /**
     * Parse documents path string to Map
     * Format: "key1:path1;key2:path2;..."
     */
    private java.util.Map<String, String> parseDocumentsPath(String documentsPath) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        if (documentsPath == null || documentsPath.isEmpty()) {
            return map;
        }
        
        String[] pairs = documentsPath.split(";");
        for (String pair : pairs) {
            if (pair.contains(":")) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    map.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        return map;
    }

    // ========== Approve/Reject Registration ==========
    @PostMapping("/students/{id}/approve")
    public String approveRegistration(@PathVariable int id, Model model) {
        try {
            studentService.approveRegistration(id);
            model.addAttribute("success", "Duyệt hồ sơ thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/students/" + id;
    }

    @PostMapping("/students/{id}/reject")
    public String rejectRegistration(@PathVariable int id, Model model) {
        try {
            studentService.rejectRegistration(id);
            model.addAttribute("success", "Từ chối hồ sơ thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/students/" + id;
    }
    
 // 1. Hiển thị form chọn tòa + phòng (GET)
    @GetMapping("/students/{id}/assign-room")
    public String showAssignRoomForm(
            @PathVariable int id,
            @RequestParam(required = false) Integer buildingId,
            Model model) {

        Students student = studentService.findById(id);
        if (student == null || student.getFloor() == null || student.getFloor().trim().isEmpty()) {
            return "redirect:/admin/students";
        }

        List<Buildings> buildings = buildingService.getAllBuildings();
        List<Rooms> availableRooms = null;

        // Chuyển floor (String) → int an toàn
        int floorInt = 6;
        try {
            floorInt = Integer.parseInt(student.getFloor().trim());
            if (floorInt < 1 || floorInt > 6) floorInt = 6;
        } catch (Exception e) {
            floorInt = 6;
        }

        Map<Integer, Long> feeMap = Map.of(
            1, 3_000_000L, 2, 2_700_000L, 3, 2_400_000L,
            4, 2_100_000L, 5, 1_800_000L, 6, 1_500_000L
        );

        if (buildingId != null) {
            final int targetFloor = floorInt;
            availableRooms = roomService.findRoomsByBuildingId(buildingId)
                    .stream()
                    .filter(r -> r.getFloor() == targetFloor)
                    .filter(r -> r.getCurrentOccupants() < r.getSlot())
                    .sorted(Comparator.comparing(Rooms::getFloor).thenComparing(Rooms::getRoomNumber))
                    .toList();
        }
        Buildings selectedBuilding = null;
        if (buildingId != null) {
            selectedBuilding = buildings.stream()
                    .filter(b -> b.getId() == buildingId)
                    .findFirst()
                    .orElse(null);
        }

        model.addAttribute("selectedBuilding", selectedBuilding);
        model.addAttribute("student", student);
        model.addAttribute("buildings", buildings);
        model.addAttribute("buildingId", buildingId);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("feeMap", feeMap);
        model.addAttribute("floorInt", floorInt);
        return "admin/student/assign-room";
    }
    // 2. Xử lý phân phòng (POST)
    @PostMapping("/students/{id}/assign-room")
    public String assignRoom(
            @PathVariable int id,
            @RequestParam int roomId,
            RedirectAttributes ra) {

        try {
            studentService.approveAndAssignRoom(id, roomId);
            ra.addFlashAttribute("success", "Phân phòng thành công! Hợp đồng đã được tạo.");
        } catch (Exception e) {
            // In lỗi ra log + hiển thị cho người dùng
            e.printStackTrace(); // <<<< QUAN TRỌNG: xem log này
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/students/" + id;
    }
    
 // QUẢN LÝ KÝ TÚC XÁ

    /** Danh sách các tòa nhà */
    @GetMapping("/dormitories")
    public String listDormitories(Model model) {
        model.addAttribute("buildings", buildingService.getAllBuildings());
        model.addAttribute("pageTitle", "Quản lý Ký túc xá");
        return "admin/dormitory/list";
    }

    /** Chi tiết tòa nhà + phòng theo tầng */
    @GetMapping("/dormitories/{id}")
    public String viewDormitoryDetail(@PathVariable("id") int buildingId, Model model) {
        Buildings building = buildingService.getBuildingById(buildingId);
        if (building == null) {
            return "redirect:/admin/dormitories";
        }

        Map<Integer, List<Rooms>> roomsByFloor = roomService.getRoomsGroupedByFloor(buildingId);
        
     // Tính tổng số phòng để gửi sang template (Thymeleaf không làm được stream)
        int totalRooms = roomsByFloor.values().stream()
                                     .mapToInt(List::size)
                                     .sum();
        model.addAttribute("building", building);
        model.addAttribute("roomsByFloor", roomsByFloor);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("pageTitle", "Tòa " + building.getName());

        return "admin/dormitory/detail";
    }
}
