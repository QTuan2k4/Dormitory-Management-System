package com.group7.DMS.controller;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.service.InvoiceService;
import com.group7.DMS.service.StudentService;
import com.group7.DMS.service.UserService;
import com.group7.DMS.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentController {
    private static final String CITIZEN_ID_REGEX = "\\d{12}";
    private static final String PNG_EXTENSION = ".png";
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter[] BIRTHDATE_FORMATTERS = new DateTimeFormatter[] {
            DISPLAY_DATE_FORMATTER,
            DateTimeFormatter.ISO_LOCAL_DATE
    };

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private ContractService contractService;

    @Value("${app.upload.base-dir:}")
    private String uploadBaseDir;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        // Lấy hợp đồng active của sinh viên
        List<com.group7.DMS.entity.Contracts> activeContracts = contractService.findActiveContractsByStudent(student.getId());
        com.group7.DMS.entity.Contracts currentContract = activeContracts.isEmpty() ? null : activeContracts.get(0);

        // Lấy danh sách hóa đơn của sinh viên
        List<Invoices> invoices = invoiceService.findByStudentId(student.getId());

        // Tính tổng tiền chưa thanh toán
        java.math.BigDecimal totalUnpaid = invoiceService.calculateTotalUnpaidAmount(student.getId());

        // Thống kê chi tiết
        long totalInvoices = invoices.size();
        long paidCount = invoices.stream().filter(i -> i.getStatus() == Invoices.InvoiceStatus.PAID).count();
        long unpaidCount = invoices.stream().filter(i -> i.getStatus() == Invoices.InvoiceStatus.UNPAID).count();
        long overdueCount = invoices.stream().filter(i -> i.getStatus() == Invoices.InvoiceStatus.OVERDUE).count();
        
        // Tổng tiền đã thanh toán
        java.math.BigDecimal totalPaid = invoices.stream()
                .filter(i -> i.getStatus() == Invoices.InvoiceStatus.PAID)
                .map(Invoices::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Lấy 5 hóa đơn gần nhất
        List<Invoices> recentInvoices = invoices.stream()
                .sorted((a, b) -> b.getIssueDate().compareTo(a.getIssueDate()))
                .limit(5)
                .toList();

        // Thông tin phòng
        com.group7.DMS.entity.Rooms room = null;
        com.group7.DMS.entity.Buildings building = null;
        if (currentContract != null && currentContract.getRoom() != null) {
            room = currentContract.getRoom();
            building = room.getBuilding();
        }

        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("currentContract", currentContract);
        model.addAttribute("room", room);
        model.addAttribute("building", building);
        model.addAttribute("invoices", recentInvoices);
        model.addAttribute("totalUnpaid", totalUnpaid);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalInvoices", totalInvoices);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("unpaidCount", unpaidCount);
        model.addAttribute("overdueCount", overdueCount);
        
        return "student/dashboard";
    }

    @GetMapping("/personal-info")
    public String personalInfo(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("student", student);

        return "student/personal-info";
    }

    @GetMapping("/edit-profile")
    public String editProfileForm(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        model.addAttribute("student", student);
        model.addAttribute("user", user);

        return "student/edit-profile";
    }

    @PostMapping("/edit-profile")
    public String updateProfile(@ModelAttribute("student") Students studentDetails, Model model, Authentication auth) {
        String username = auth.getName();
        Users currentUser = userService.findByUsername(username);
        Students currentStudent = studentService.findByUserId(currentUser.getId());

        if (currentStudent != null) {
            currentStudent.setFullName(studentDetails.getFullName());
            currentStudent.setPhone(studentDetails.getPhone());
            currentStudent.setAddress(studentDetails.getAddress());
            currentStudent.setStudentClass(studentDetails.getStudentClass());
            currentStudent.setCourse(studentDetails.getCourse());
            currentStudent.setMajor(studentDetails.getMajor());

            studentService.update(currentStudent);
        }
        return "redirect:/student/personal-info";
    }

    @GetMapping("/register-personal-info")
    public String registerPersonalInfoForm(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        if (student != null && student.getDocumentsPath() != null) {
            model.addAttribute("documentMap", parseDocumentsPath(student.getDocumentsPath()));
        }

        model.addAttribute("user", user);
        model.addAttribute("student", student);
        return "student/register-personal-info";
    }

    private Map<String, String> parseDocumentsPath(String documentsPath) {
        Map<String, String> map = new HashMap<>();
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


    @PostMapping("/register-personal-info")
    public String registerPersonalInfo(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String birthDate,
            @RequestParam(required = false) String citizenId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String studentClass,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String[] discountTypes,
            @RequestParam(required = false) MultipartFile idCardFront,
            @RequestParam(required = false) MultipartFile idCardBack,
            @RequestParam(required = false) MultipartFile congenitalDefectFile,
            @RequestParam(required = false) MultipartFile difficultAreaFile,
            @RequestParam(required = false) MultipartFile poorHouseholdFile,
            @RequestParam(required = false) MultipartFile revolutionaryFamilyFile,
            @RequestParam(required = false) MultipartFile ethnicMinorityFile,
            Authentication auth,
            Model model) {

        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        if (student == null) {
            model.addAttribute("error", "Không tìm thấy thông tin sinh viên!");
            return "student/register-personal-info";
        }

        try {
            String sanitizedCitizenId = citizenId != null ? citizenId.trim() : null;
            validateCitizenId(sanitizedCitizenId);
            student.setCitizenId(sanitizedCitizenId);

            if (fullName != null && !fullName.isEmpty()) {
                student.setFullName(fullName);
            }
            student.setBirthDate(parseBirthDate(birthDate));
            if (studentClass != null && !studentClass.isEmpty()) {
                student.setStudentClass(studentClass);
            }
            if (address != null && !address.isEmpty()) {
                student.setAddress(address);
            }
            if (gender != null && !gender.isEmpty()) {
                student.setGender(gender);
            }
            if (floor != null && !floor.isEmpty()) {
                student.setFloor(floor);
            }

            Map<String, String> documentMap = new HashMap<>();
            if (student.getDocumentsPath() != null && !student.getDocumentsPath().isEmpty()) {
                documentMap = parseDocumentsPath(student.getDocumentsPath());
            }

            if (idCardFront != null && !idCardFront.isEmpty()) {
                ensurePngFile(idCardFront, "Ảnh căn cước mặt trước");
                String path = saveFile(idCardFront, "idcards");
                documentMap.put("idCardFront", path);
            }
            if (idCardBack != null && !idCardBack.isEmpty()) {
                ensurePngFile(idCardBack, "Ảnh căn cước mặt sau");
                String path = saveFile(idCardBack, "idcards");
                documentMap.put("idCardBack", path);
            }

            if (congenitalDefectFile != null && !congenitalDefectFile.isEmpty()) {
                ensurePngFile(congenitalDefectFile, "File khiếm khuyết bẩm sinh");
                String path = saveFile(congenitalDefectFile, "discounts");
                documentMap.put("congenitalDefect", path);
            }
            if (difficultAreaFile != null && !difficultAreaFile.isEmpty()) {
                ensurePngFile(difficultAreaFile, "File vùng đặc biệt khó khăn");
                String path = saveFile(difficultAreaFile, "discounts");
                documentMap.put("difficultArea", path);
            }
            if (poorHouseholdFile != null && !poorHouseholdFile.isEmpty()) {
                ensurePngFile(poorHouseholdFile, "File hộ nghèo/cận nghèo");
                String path = saveFile(poorHouseholdFile, "discounts");
                documentMap.put("poorHousehold", path);
            }
            if (revolutionaryFamilyFile != null && !revolutionaryFamilyFile.isEmpty()) {
                ensurePngFile(revolutionaryFamilyFile, "File con nhà cách mạng");
                String path = saveFile(revolutionaryFamilyFile, "discounts");
                documentMap.put("revolutionaryFamily", path);
            }
            if (ethnicMinorityFile != null && !ethnicMinorityFile.isEmpty()) {
                ensurePngFile(ethnicMinorityFile, "File dân tộc thiểu số");
                String path = saveFile(ethnicMinorityFile, "discounts");
                documentMap.put("ethnicMinority", path);
            }

            if (!documentMap.isEmpty()) {
                StringBuilder documentsPathBuilder = new StringBuilder();
                for (Map.Entry<String, String> entry : documentMap.entrySet()) {
                    if (documentsPathBuilder.length() > 0) {
                        documentsPathBuilder.append(";");
                    }
                    documentsPathBuilder.append(entry.getKey()).append(":").append(entry.getValue());
                }
                student.setDocumentsPath(documentsPathBuilder.toString());
            }

            if (student.getRegistrationStatus() == null ||
                student.getRegistrationStatus().equals(Students.RegistrationStatus.NOT_SUBMITTED) ||
                student.getRegistrationStatus().equals(Students.RegistrationStatus.REJECTED)) {
                student.setRegistrationStatus(Students.RegistrationStatus.PENDING);
                student.setApplicationDate(LocalDate.now());
            }

            studentService.update(student);
            model.addAttribute("success", "Cập nhật thông tin thành công!");

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }

        if (student.getDocumentsPath() != null) {
            model.addAttribute("documentMap", parseDocumentsPath(student.getDocumentsPath()));
        }
        model.addAttribute("user", user);
        model.addAttribute("student", student);

        return "student/register-personal-info";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, Model model, Authentication auth) {
        try {
            String username = auth.getName();
            Users user = userService.findByUsername(username);
            Students student = studentService.findByUserId(user.getId());

            model.addAttribute("user", user);
            model.addAttribute("student", student);
            model.addAttribute("error", "Kích thước file quá lớn! Vui lòng chọn file nhỏ hơn 50MB.");
        } catch (Exception e) {
            model.addAttribute("error", "Kích thước file quá lớn! Vui lòng chọn file nhỏ hơn 50MB.");
        }
        return "student/register-personal-info";
    }

    private LocalDate parseBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            throw new IllegalArgumentException("Ngày sinh không được bỏ trống.");
        }
        String trimmed = birthDate.trim();
        for (DateTimeFormatter formatter : BIRTHDATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Ngày sinh không hợp lệ. Vui lòng nhập theo định dạng dd-MM-yyyy.");
    }

    private void validateCitizenId(String citizenId) {
        if (citizenId == null || !citizenId.matches(CITIZEN_ID_REGEX)) {
            throw new IllegalArgumentException("Căn cước công dân phải gồm đúng 12 chữ số.");
        }
    }

    private void ensurePngFile(MultipartFile file, String fieldLabel) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(PNG_EXTENSION)) {
            throw new IllegalArgumentException(fieldLabel + " phải là file PNG (.png).");
        }
    }

    private String saveFile(MultipartFile file, String subfolder) throws IOException {
        String baseDir;
        if (uploadBaseDir != null && !uploadBaseDir.isEmpty()) {
            baseDir = uploadBaseDir;
        } else {
            baseDir = getProjectRootDirectory();
        }

        Path uploadPath = Paths.get(baseDir, "uploads", subfolder);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(fileName);

        Files.write(filePath, file.getBytes());

        return "uploads/" + subfolder + "/" + fileName;
    }

    private String getProjectRootDirectory() {
        String currentDir = System.getProperty("user.dir");
        File dir = new File(currentDir);

        if (new File(dir, "pom.xml").exists() || new File(dir, "src").exists()) {
            return currentDir;
        }

        File parent = dir.getParentFile();
        if (parent != null) {
            if (new File(parent, "pom.xml").exists() || new File(parent, "src").exists()) {
                return parent.getAbsolutePath();
            }
        }

        File current = dir;
        int maxDepth = 5;
        while (current != null && maxDepth > 0) {
            if (current.getName().equals("Dormitory-Management-System") ||
                new File(current, "pom.xml").exists()) {
                return current.getAbsolutePath();
            }
            current = current.getParentFile();
            maxDepth--;
        }

        return currentDir;
    }

    @Autowired
    private com.group7.DMS.service.RoomService roomService;

    @GetMapping("/room-info")
    public String roomInfo(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("student", student);

        // Lấy hợp đồng active của sinh viên
        List<com.group7.DMS.entity.Contracts> activeContracts = contractService.findActiveContractsByStudent(student.getId());
        
        if (activeContracts.isEmpty()) {
            model.addAttribute("hasRoom", false);
        } else {
            com.group7.DMS.entity.Contracts currentContract = activeContracts.get(0);
            com.group7.DMS.entity.Rooms room = currentContract.getRoom();
            
            if (room == null || room.getId() == 0) {
                model.addAttribute("hasRoom", false);
            } else {
                model.addAttribute("hasRoom", true);
                model.addAttribute("room", room);
                model.addAttribute("building", room.getBuilding());
                model.addAttribute("contract", currentContract);
                
                // Lấy danh sách bạn cùng phòng (các sinh viên khác trong cùng phòng)
                List<com.group7.DMS.entity.Contracts> roomContracts = contractService.findActiveContractsByRoom(room.getId());
                List<Students> roomMates = roomContracts.stream()
                        .map(com.group7.DMS.entity.Contracts::getStudent)
                        .filter(s -> s.getId() != student.getId()) // Loại bỏ chính sinh viên hiện tại
                        .toList();
                model.addAttribute("roomMates", roomMates);
            }
        }

        return "student/room-info";
    }

    @GetMapping("/invoices")
    public String invoiceList(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        List<Invoices> invoices = invoiceService.findByStudentId(student.getId());

        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("invoices", invoices);
        return "student/invoice-list";
    }

    @GetMapping("/invoices/{id}/payment")
    public String paymentPage(@PathVariable int id, Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        Invoices invoice = invoiceService.findById(id);
        if (invoice != null && invoice.getContract().getStudent().getId() == student.getId()) {
            // Redirect đến trang checkout với 3 phương thức thanh toán
            return "redirect:/payment/checkout/" + id;
        }
        return "redirect:/student/invoices";
    }

    @PostMapping("/invoices/{id}/payment")
    public String processPayment(@PathVariable int id,
                                @RequestParam String paymentMethod,
                                @RequestParam String transactionId,
                                Authentication auth,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        Invoices invoice = invoiceService.findById(id);

        if (invoice == null || invoice.getContract().getStudent().getId() != student.getId()) {
            ra.addFlashAttribute("error", "Không tìm thấy hóa đơn hoặc bạn không có quyền thanh toán!");
            return "redirect:/student/invoices";
        }

        if (invoice.getStatus() == Invoices.InvoiceStatus.PAID) {
            ra.addFlashAttribute("error", "Hóa đơn này đã được thanh toán trước đó!");
            return "redirect:/student/invoices";
        }

        try {
            Payments.PaymentMethod method = Payments.PaymentMethod.valueOf(paymentMethod.toUpperCase());
            invoiceService.processPayment(id, invoice.getTotalAmount(), method, transactionId);
            ra.addFlashAttribute("success", "Thanh toán thành công!");
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            ra.addFlashAttribute("error", "Hóa đơn đã được cập nhật bởi người khác. Vui lòng thử lại!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/student/invoices";
    }
}
