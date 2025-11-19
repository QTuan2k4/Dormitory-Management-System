package com.group7.DMS.controller;

import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.service.StudentService;
import com.group7.DMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Controller
@RequestMapping("/student")
public class StudentRegistrationController {
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private UserService userService;
    
    private static final String UPLOAD_DIR = "uploads/documents/";
    
    // ========== Registration Form ==========
    @GetMapping("/registration")
    public String registrationForm(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        
        // Kiểm tra xem sinh viên đã có hồ sơ chưa
        Students existingStudent = studentService.findByUserId(user.getId());
        
        if (existingStudent != null) {
            // Nếu đã có hồ sơ, redirect tới dashboard
            return "redirect:/student/status";
        }
        
        model.addAttribute("user", user);
        return "student/registration";
    }
    
    // ========== Submit Registration ==========
    @PostMapping("/registration")
    public String submitRegistration(
            @RequestParam String fullName,
            @RequestParam String studentId,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String birthDate,
            @RequestParam(required = false) MultipartFile document,
            Authentication auth,
            Model model) {
        
        try {
            Users user = userService.findByUsername(auth.getName());
            
            // Kiểm tra studentId đã tồn tại chưa
            if (studentService.findByStudentId(studentId) != null) {
                model.addAttribute("error", "Mã sinh viên này đã được đăng ký!");
                return "student/registration";
            }
            
            // Tạo Students object
            Students student = new Students();
            student.setUser(user);
            student.setFullName(fullName);
            student.setStudentId(studentId);
            student.setPhone(phone);
            student.setAddress(address);
            student.setBirthDate(LocalDate.parse(birthDate));
            student.setRegistrationStatus(Students.RegistrationStatus.PENDING);
            
            // Upload document
            if (document != null && !document.isEmpty()) {
                String fileName = saveDocument(document);
                student.setDocumentsPath(fileName);
            }
            
            // Lưu vào database
            studentService.save(student);
            
            return "redirect:/student/status";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "student/registration";
        }
    }
    
    // ========== View Status ==========
    @GetMapping("/status")
    public String viewStatus(Authentication auth, Model model) {
        Users user = userService.findByUsername(auth.getName());
        Students student = studentService.findByUserId(user.getId());
        
        if (student == null) {
            return "redirect:/student/registration";
        }
        
        model.addAttribute("student", student);
        model.addAttribute("user", user);
        return "student/status";
    }
    
    // ========== Update Registration (Re-submit) ==========
    @PostMapping("/registration/{id}/update")
    public String updateRegistration(
            @PathVariable int id,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String birthDate,
            @RequestParam(required = false) MultipartFile document,
            Model model) {
        
        try {
            Students student = studentService.findById(id);
            
            if (student == null) {
                model.addAttribute("error", "Hồ sơ không tìm thấy!");
                return "redirect:/student/status";
            }
            
            // Chỉ cho phép cập nhật nếu trạng thái là PENDING hoặc REJECTED
            if (!student.getRegistrationStatus().equals(Students.RegistrationStatus.PENDING) &&
                !student.getRegistrationStatus().equals(Students.RegistrationStatus.REJECTED)) {
                model.addAttribute("error", "Không thể cập nhật hồ sơ đã duyệt!");
                return "redirect:/student/status";
            }
            
            // Cập nhật thông tin
            student.setFullName(fullName);
            student.setPhone(phone);
            student.setAddress(address);
            student.setBirthDate(LocalDate.parse(birthDate));
            student.setRegistrationStatus(Students.RegistrationStatus.PENDING); // Reset về PENDING
            
            // Upload document mới
            if (document != null && !document.isEmpty()) {
                String fileName = saveDocument(document);
                student.setDocumentsPath(fileName);
            }
            
            studentService.update(student);
            
            return "redirect:/student/status";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/student/status";
        }
    }
    
    // ========== Helper: Save Document ==========
    private String saveDocument(MultipartFile file) throws IOException {
        // Tạo thư mục nếu không tồn tại
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(fileName);
        
        // Save file
        Files.write(filePath, file.getBytes());
        
        return "uploads/documents/" + fileName;
    }
}
