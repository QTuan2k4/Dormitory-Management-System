package com.group7.DMS.controller;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.service.InvoiceService;
import com.group7.DMS.service.StudentService;
import com.group7.DMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private StudentService studentService;

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
    public String listStudents(Model model,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String status,
                               Authentication auth) {
        List<Students> students = studentService.findAll();

        // Filter by status if provided
        if (status != null && !status.isBlank()) {
            try {
                Students.RegistrationStatus regStatus = Students.RegistrationStatus.valueOf(status.toUpperCase());
                students = studentService.findByRegistrationStatus(regStatus);
            } catch (IllegalArgumentException e) {
                // invalid status - ignore filtering by status
            }
        }

        // Filter by search if provided (search in full name)
        if (search != null && !search.isBlank()) {
            List<Students> byName = studentService.findByFullNameContaining(search);
            // intersect results if status filter was applied
            students.retainAll(byName);
            model.addAttribute("search", search);
        }

        // Add username for template fallback
        if (auth != null) {
            model.addAttribute("username", auth.getName());
        }

        model.addAttribute("students", students);
        model.addAttribute("status", status);
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
            model.addAttribute("student", student);
            return "admin/student-detail";
        }
        return "redirect:/admin/students";
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
}
