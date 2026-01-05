package com.group7.DMS.controller;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.service.InvoiceService;
import com.group7.DMS.service.StudentService;
import com.group7.DMS.service.UserService;
import com.group7.DMS.service.payment.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    /**
     * Trang chọn phương thức thanh toán
     */
    @GetMapping("/checkout/{invoiceId}")
    public String checkoutPage(@PathVariable int invoiceId, Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        Invoices invoice = invoiceService.findById(invoiceId);
        if (invoice == null || invoice.getContract().getStudent().getId() != student.getId()) {
            return "redirect:/student/invoices";
        }

        if (invoice.getStatus() == Invoices.InvoiceStatus.PAID) {
            return "redirect:/student/invoices";
        }

        model.addAttribute("invoice", invoice);
        model.addAttribute("student", student);
        return "student/payment-checkout";
    }

    /**
     * Tạo thanh toán VNPay
     */
    @PostMapping("/vnpay/create")
    public String createVNPayPayment(@RequestParam int invoiceId, HttpServletRequest request, 
                                      Authentication auth, RedirectAttributes ra) {
        try {
            Invoices invoice = validateInvoiceOwnership(invoiceId, auth);
            if (invoice == null) {
                ra.addFlashAttribute("error", "Không tìm thấy hóa đơn!");
                return "redirect:/student/invoices";
            }

            String ipAddress = getClientIP(request);
            long amount = invoice.getTotalAmount().longValue();
            String orderInfo = "Thanh toan hoa don " + invoice.getInvoiceNumber();

            String paymentUrl = vnPayService.createPaymentUrl(invoiceId, amount, orderInfo, ipAddress);
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi tạo thanh toán VNPay: " + e.getMessage());
            return "redirect:/payment/checkout/" + invoiceId;
        }
    }

    /**
     * Callback từ VNPay
     */
    @GetMapping("/vnpay/callback")
    public String vnpayCallback(@RequestParam Map<String, String> params, Model model) {
        try {
            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            String transactionId = params.get("vnp_TransactionNo");
            String orderInfo = params.get("vnp_OrderInfo");
            String paymentTime = params.get("vnp_PayDate");
            String totalPrice = params.get("vnp_Amount");

            // Log để debug
            System.out.println("VNPay Callback - ResponseCode: " + responseCode);
            System.out.println("VNPay Callback - TxnRef: " + txnRef);
            System.out.println("VNPay Callback - TransactionId: " + transactionId);

            int invoiceId = vnPayService.extractInvoiceId(txnRef);
            Invoices invoice = invoiceService.findById(invoiceId);

            if (invoice == null) {
                model.addAttribute("errorMessage", "Không tìm thấy hóa đơn!");
                return "student/payment-fail";
            }

            if ("00".equals(responseCode)) {
                // Kiểm tra hóa đơn đã thanh toán chưa
                if (invoice.getStatus() != Invoices.InvoiceStatus.PAID) {
                    // Thanh toán thành công
                    invoiceService.processPayment(invoiceId, invoice.getTotalAmount(), 
                            Payments.PaymentMethod.VNPAY, transactionId);
                    System.out.println("VNPay Callback - Payment processed successfully for invoice: " + invoiceId);
                }
                
                // Format lại số tiền (VNPay trả về đã nhân 100)
                long amount = Long.parseLong(totalPrice) / 100;
                String formattedAmount = String.format("%,d", amount);
                
                // Format thời gian thanh toán
                String formattedTime = formatVNPayDate(paymentTime);
                
                model.addAttribute("orderId", orderInfo);
                model.addAttribute("totalPrice", formattedAmount);
                model.addAttribute("transactionId", transactionId);
                model.addAttribute("paymentTime", formattedTime);
                
                return "student/payment-success";
            } else {
                model.addAttribute("errorMessage", "Thanh toán VNPay thất bại! Mã lỗi: " + responseCode);
                model.addAttribute("invoiceId", invoiceId);
                return "student/payment-fail";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi xử lý callback: " + e.getMessage());
            return "student/payment-fail";
        }
    }

    /**
     * Thanh toán tiền mặt - hiển thị thông tin hướng dẫn
     */
    @PostMapping("/cash/create")
    public String createCashPayment(@RequestParam int invoiceId, Authentication auth, 
                                     Model model, RedirectAttributes ra) {
        try {
            Invoices invoice = validateInvoiceOwnership(invoiceId, auth);
            if (invoice == null) {
                ra.addFlashAttribute("error", "Không tìm thấy hóa đơn!");
                return "redirect:/student/invoices";
            }

            model.addAttribute("invoice", invoice);
            return "student/payment-cash";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/payment/checkout/" + invoiceId;
        }
    }

    /**
     * Format ngày giờ từ VNPay (yyyyMMddHHmmss) sang dd/MM/yyyy HH:mm:ss
     */
    private String formatVNPayDate(String vnpayDate) {
        if (vnpayDate == null || vnpayDate.length() != 14) {
            return vnpayDate;
        }
        try {
            String year = vnpayDate.substring(0, 4);
            String month = vnpayDate.substring(4, 6);
            String day = vnpayDate.substring(6, 8);
            String hour = vnpayDate.substring(8, 10);
            String minute = vnpayDate.substring(10, 12);
            String second = vnpayDate.substring(12, 14);
            return day + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + second;
        } catch (Exception e) {
            return vnpayDate;
        }
    }

    // Helper methods
    private Invoices validateInvoiceOwnership(int invoiceId, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());

        Invoices invoice = invoiceService.findById(invoiceId);
        if (invoice == null || invoice.getContract().getStudent().getId() != student.getId()) {
            return null;
        }
        if (invoice.getStatus() == Invoices.InvoiceStatus.PAID) {
            return null;
        }
        return invoice;
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
