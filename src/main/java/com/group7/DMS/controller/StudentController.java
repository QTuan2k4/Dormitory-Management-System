package com.group7.DMS.controller;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.service.InvoiceService;
import com.group7.DMS.service.StudentService;
import com.group7.DMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Value("${app.upload.base-dir:}")
    private String uploadBaseDir;


    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("student", student);
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
        
        // Parse documents path to map
        if (student != null && student.getDocumentsPath() != null) {
            model.addAttribute("documentMap", parseDocumentsPath(student.getDocumentsPath()));
        }
        
        model.addAttribute("user", user);
        model.addAttribute("student", student);
        return "student/register-personal-info";
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
        
        try {
            String username = auth.getName();
            Users user = userService.findByUsername(username);
            Students student = studentService.findByUserId(user.getId());
            
            if (student == null) {
                model.addAttribute("error", "Không tìm thấy thông tin sinh viên!");
                return "student/register-personal-info";
            }
            
            // Update student information
            if (fullName != null && !fullName.isEmpty()) {
                student.setFullName(fullName);
            }
            if (birthDate != null && !birthDate.isEmpty()) {
                student.setBirthDate(LocalDate.parse(birthDate));
            }
            if (studentClass != null && !studentClass.isEmpty()) {
                student.setStudentClass(studentClass);
            }
            if (address != null && !address.isEmpty()) {
                student.setAddress(address);
            }
            if (gender != null && !gender.isEmpty()) {
                student.setGender(gender);
            }
            if (citizenId != null && !citizenId.isEmpty()) {
                student.setCitizenId(citizenId);
            }
            if (floor != null && !floor.isEmpty()) {
                student.setFloor(floor);
            }
            
            // Save uploaded files - merge with existing documents (replace if key exists)
            java.util.Map<String, String> documentMap = new java.util.HashMap<>();
            
            // Load existing documents into map
            if (student.getDocumentsPath() != null && !student.getDocumentsPath().isEmpty()) {
                documentMap = parseDocumentsPath(student.getDocumentsPath());
            }
            
            // ID Card files - replace if exists
            if (idCardFront != null && !idCardFront.isEmpty()) {
                String path = saveFile(idCardFront, "idcards");
                documentMap.put("idCardFront", path);
            }
            if (idCardBack != null && !idCardBack.isEmpty()) {
                String path = saveFile(idCardBack, "idcards");
                documentMap.put("idCardBack", path);
            }
            
            // Discount policy files - replace if exists
            if (congenitalDefectFile != null && !congenitalDefectFile.isEmpty()) {
                String path = saveFile(congenitalDefectFile, "discounts");
                documentMap.put("congenitalDefect", path);
            }
            if (difficultAreaFile != null && !difficultAreaFile.isEmpty()) {
                String path = saveFile(difficultAreaFile, "discounts");
                documentMap.put("difficultArea", path);
            }
            if (poorHouseholdFile != null && !poorHouseholdFile.isEmpty()) {
                String path = saveFile(poorHouseholdFile, "discounts");
                documentMap.put("poorHousehold", path);
            }
            if (revolutionaryFamilyFile != null && !revolutionaryFamilyFile.isEmpty()) {
                String path = saveFile(revolutionaryFamilyFile, "discounts");
                documentMap.put("revolutionaryFamily", path);
            }
            if (ethnicMinorityFile != null && !ethnicMinorityFile.isEmpty()) {
                String path = saveFile(ethnicMinorityFile, "discounts");
                documentMap.put("ethnicMinority", path);
            }
            
            // Convert map back to string format: "key1:path1;key2:path2;..."
            if (!documentMap.isEmpty()) {
                StringBuilder documentsPathBuilder = new StringBuilder();
                for (java.util.Map.Entry<String, String> entry : documentMap.entrySet()) {
                    if (documentsPathBuilder.length() > 0) {
                        documentsPathBuilder.append(";");
                    }
                    documentsPathBuilder.append(entry.getKey()).append(":").append(entry.getValue());
                }
                student.setDocumentsPath(documentsPathBuilder.toString());
            }
            
            // Đánh dấu hồ sơ cần duyệt nếu đây là lần gửi đầu tiên hoặc sau khi bị từ chối
            if (student.getRegistrationStatus() == null ||
                student.getRegistrationStatus().equals(Students.RegistrationStatus.NOT_SUBMITTED) ||
                student.getRegistrationStatus().equals(Students.RegistrationStatus.REJECTED)) {
                student.setRegistrationStatus(Students.RegistrationStatus.PENDING);
                student.setApplicationDate(LocalDate.now());
            }
            
            studentService.update(student);
            
            // Parse documents path to map
            if (student.getDocumentsPath() != null) {
                model.addAttribute("documentMap", parseDocumentsPath(student.getDocumentsPath()));
            }
            
            model.addAttribute("success", "Cập nhật thông tin thành công!");
            model.addAttribute("user", user);
            model.addAttribute("student", student);
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }
        
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
    
    private String saveFile(MultipartFile file, String subfolder) throws IOException {
        // Get upload base directory
        String baseDir;
        if (uploadBaseDir != null && !uploadBaseDir.isEmpty()) {
            // Use configured directory (absolute path)
            baseDir = uploadBaseDir;
        } else {
            // Find project root directory
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
        
        // Return relative path for web access
        return "uploads/" + subfolder + "/" + fileName;
    }
    
    /**
     * Get project root directory by finding pom.xml or src folder
     * This avoids System32 issue when user.dir is wrong
     */
    private String getProjectRootDirectory() {
        // Try user.dir first
        String currentDir = System.getProperty("user.dir");
        File dir = new File(currentDir);
        
        // Check if pom.xml or src folder exists (project root indicators)
        if (new File(dir, "pom.xml").exists() || new File(dir, "src").exists()) {
            return currentDir;
        }
        
        // If not, try going up one level
        File parent = dir.getParentFile();
        if (parent != null) {
            if (new File(parent, "pom.xml").exists() || new File(parent, "src").exists()) {
                return parent.getAbsolutePath();
            }
        }
        
        // Try to find project root by looking for Dormitory-Management-System folder
        File current = dir;
        int maxDepth = 5; // Prevent infinite loop
        while (current != null && maxDepth > 0) {
            if (current.getName().equals("Dormitory-Management-System") || 
                new File(current, "pom.xml").exists()) {
                return current.getAbsolutePath();
            }
            current = current.getParentFile();
            maxDepth--;
        }
        
        // Fallback: use current working directory
        return currentDir;
    }

    @GetMapping("/room-info")
    public String roomInfo(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("student", student);
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
            model.addAttribute("user", user);
            model.addAttribute("student", student);
            model.addAttribute("invoice", invoice);
            return "student/payment";
        }
        return "redirect:/student/invoices";
    }

    @PostMapping("/invoices/{id}/payment")
    public String processPayment(@PathVariable int id, 
                                @RequestParam String paymentMethod,
                                @RequestParam String transactionId,
                                Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());
        
        Invoices invoice = invoiceService.findById(id);
        if (invoice != null && invoice.getContract().getStudent().getId() == student.getId()) {
            Payments.PaymentMethod method = Payments.PaymentMethod.valueOf(paymentMethod.toUpperCase());
            invoiceService.processPayment(id, invoice.getTotalAmount(), method, transactionId);
        }
        return "redirect:/student/invoices";
    }
 
}