package com.group7.DMS.controller;

import com.group7.DMS.entity.Users;
import com.group7.DMS.service.UserService;
import com.group7.DMS.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.util.StringUtils;

@Controller
public class LoginController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private StudentService studentService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("supportEmail", "ktx.support@qnu.edu.vn");
        model.addAttribute("supportPhone", "0256 123 4567");
        model.addAttribute("supportOffice", "Phòng Quản lý KTX - Tòa nhà A1");
        return "forgot-password";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, 
                           @RequestParam(required = false) String logout, 
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
        }
        if (logout != null) {
            model.addAttribute("message", "Đăng xuất thành công");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("users", new Users());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                            @RequestParam String studentId,
                            @RequestParam String course,
                            @RequestParam String major,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam String confirmPassword,
                            Model model, // Dùng Model để giữ lại dữ liệu khi lỗi
                            RedirectAttributes redirectAttributes) { // Giữ lại RedirectAttributes cho trường hợp thành công
        
        // 1. Chuyển các giá trị đã nhập vào Model để giữ lại trên form khi có lỗi
        model.addAttribute("fullName", fullName);
        model.addAttribute("studentId", studentId);
        model.addAttribute("course", course);
        model.addAttribute("major", major);
        model.addAttribute("email", email);

        try {
            // Basic validations
            if (!StringUtils.hasText(fullName) || !StringUtils.hasText(studentId) ||
                !StringUtils.hasText(course) || !StringUtils.hasText(major) ||
                !StringUtils.hasText(email) || !StringUtils.hasText(password) ||
                !StringUtils.hasText(confirmPassword)) {
                
                model.addAttribute("error", "Vui lòng điền đầy đủ tất cả thông tin.");
                return "register"; // FORWARD
            }

            // Chỉ chứa chữ số
            if (!studentId.matches("\\d+")) {
                model.addAttribute("error", "Mã sinh viên chỉ được chứa chữ số.");
                return "register";
            }

            // Phải đúng 10 chữ số
            if (studentId.length() != 10) {
                model.addAttribute("error", "Mã sinh viên phải gồm đúng 10 chữ số.");
                return "register";
            }

            
            // 3. Kiểm tra Mật khẩu
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu và xác nhận mật khẩu không khớp.");
                return "register"; // FORWARD
            }

            if (password.length() < 6) {
                model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
                return "register"; // FORWARD
            }

            // 4. Kiểm tra sự tồn tại (Existence Check)
            if (userService.existsByUsername(studentId)) {
                model.addAttribute("error", "Mã sinh viên này đã được sử dụng để đăng nhập.");
                return "register"; // FORWARD
            }
            if (studentService.findByStudentId(studentId) != null) {
                model.addAttribute("error", "Mã sinh viên này đã được đăng ký.");
                return "register"; // FORWARD
            }
            if (userService.existsByEmail(email)) {
                model.addAttribute("error", "Email đã tồn tại.");
                return "register"; // FORWARD
            }

            // --- XỬ LÝ THÀNH CÔNG ---
            // Create user with studentId as username
            Users user = userService.createUser(studentId, email, password, Users.Role.STUDENT);

            // Create student profile with course/major stored in available fields
            studentService.createStudent(user, fullName, studentId, course, major);

            // Chuyển hướng thành công (vẫn dùng RedirectAttributes)
            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.");
            return "redirect:/login"; // REDIRECT
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "register"; // FORWARD
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        
        model.addAttribute("user", user);
        
        // Redirect based on role
        if (user.getRole() == Users.Role.ADMIN) {
            return "redirect:/admin/dashboard";
        } else if (user.getRole() == Users.Role.STAFF) {
            return "redirect:/staff/dashboard";
        } else {
            return "redirect:/student/dashboard";
        }
    }
}