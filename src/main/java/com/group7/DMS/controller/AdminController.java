package com.group7.DMS.controller;

import com.group7.DMS.entity.Invoice;
import com.group7.DMS.entity.Payment;
import com.group7.DMS.entity.User;
import com.group7.DMS.service.InvoiceService;
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

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        String username = auth.getName();
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "admin/dashboard";
    }

    @GetMapping("/invoices")
    public String invoiceList(Model model) {
        List<Invoice> invoices = invoiceService.findAll();
        model.addAttribute("invoices", invoices);
        return "admin/invoice-list";
    }

    @GetMapping("/invoices/{id}/pay")
    public String payInvoice(@PathVariable int id, Model model) {
        Invoice invoice = invoiceService.findById(id);
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
        Invoice invoice = invoiceService.findById(id);
        if (invoice != null) {
            Payment.PaymentMethod method = Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase());
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
}
