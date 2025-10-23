package com.group7.DMS.controller;

import com.group7.DMS.entity.Invoice;
import com.group7.DMS.entity.Payment;
import com.group7.DMS.entity.Student;
import com.group7.DMS.entity.User;
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
@RequestMapping("/student")
public class StudentController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        String username = auth.getName();
        User user = userService.findByUsername(username);
        Student student = studentService.findByUserId(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("student", student);
        return "student/dashboard";
    }

    @GetMapping("/room-info")
    public String roomInfo(Model model, Authentication auth) {
        String username = auth.getName();
        User user = userService.findByUsername(username);
        Student student = studentService.findByUserId(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("student", student);
        return "student/room-info";
    }

    @GetMapping("/invoices")
    public String invoiceList(Model model, Authentication auth) {
        String username = auth.getName();
        User user = userService.findByUsername(username);
        Student student = studentService.findByUserId(user.getId());
        
        List<Invoice> invoices = invoiceService.findByStudentId(student.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("invoices", invoices);
        return "student/invoice-list";
    }

    @GetMapping("/invoices/{id}/payment")
    public String paymentPage(@PathVariable int id, Model model, Authentication auth) {
        String username = auth.getName();
        User user = userService.findByUsername(username);
        Student student = studentService.findByUserId(user.getId());
        
        Invoice invoice = invoiceService.findById(id);
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
        User user = userService.findByUsername(username);
        Student student = studentService.findByUserId(user.getId());
        
        Invoice invoice = invoiceService.findById(id);
        if (invoice != null && invoice.getContract().getStudent().getId() == student.getId()) {
            Payment.PaymentMethod method = Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase());
            invoiceService.processPayment(id, invoice.getTotalAmount(), method, transactionId);
        }
        return "redirect:/student/invoices";
    }
}
