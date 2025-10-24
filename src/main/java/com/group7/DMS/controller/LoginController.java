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
    public String register(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String fullName,
                          @RequestParam String studentId,
                          @RequestParam(required = false) String phone,
                          @RequestParam(required = false) String address,
                          RedirectAttributes redirectAttributes) {
        try {
            // Check if username or email already exists
            if (userService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại");
                return "redirect:/register";
            }
            if (userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Email đã tồn tại");
                return "redirect:/register";
            }

            // Create user
            Users user = userService.createUser(username, email, password, Users.Role.STUDENT);
            
            // Create student profile
            studentService.createStudent(user, fullName, studentId, phone, address);
            
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đợi phê duyệt.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/register";
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
