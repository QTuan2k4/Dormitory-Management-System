package com.group7.DMS.controller;

import com.group7.DMS.entity.User;
import com.group7.DMS.service.UserService;
import jakarta.servlet.http.HttpSession; // Spring Boot 3 / Jakarta
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    // Vào root sẽ chuyển tới /login (tránh 404 ở "/")
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // templates/login.html
    }

    // PRG + lưu user vào session, tránh F5 bị resubmit
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("currentUser", user);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu!");
            return "login";
        }
    }

    // Trang chính sau đăng nhập
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "home"; // templates/home.html
    }

    // Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
