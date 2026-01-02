package com.group7.DMS.controller;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.service.InvoiceService;
import com.group7.DMS.service.StudentService;
import com.group7.DMS.service.UserService;
import com.group7.DMS.service.payment.MoMoService;
import com.group7.DMS.service.payment.VNPayService;
import com.group7.DMS.service.payment.ZaloPayService;
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
    private MoMoService moMoService;

    @Autowired
    private ZaloPayService zaloPayService;

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
    public String vnpayCallback(@RequestParam Map<String, String> params, RedirectAttributes ra) {
        try {
            if (!vnPayService.validateCallback(new HashMap<>(params))) {
                ra.addFlashAttribute("error", "Chữ ký không hợp lệ!");
                return "redirect:/student/invoices";
            }

            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            String transactionId = params.get("vnp_TransactionNo");

            int invoiceId = vnPayService.extractInvoiceId(txnRef);
            Invoices invoice = invoiceService.findById(invoiceId);

            if ("00".equals(responseCode)) {
                // Thanh toán thành công
                invoiceService.processPayment(invoiceId, invoice.getTotalAmount(), 
                        Payments.PaymentMethod.VNPAY, transactionId);
                ra.addFlashAttribute("success", "Thanh toán VNPay thành công!");
            } else {
                ra.addFlashAttribute("error", "Thanh toán VNPay thất bại! Mã lỗi: " + responseCode);
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi xử lý callback: " + e.getMessage());
        }
        return "redirect:/student/invoices";
    }

    /**
     * Tạo thanh toán MoMo
     */
    @PostMapping("/momo/create")
    public String createMoMoPayment(@RequestParam int invoiceId, Authentication auth, RedirectAttributes ra) {
        try {
            Invoices invoice = validateInvoiceOwnership(invoiceId, auth);
            if (invoice == null) {
                ra.addFlashAttribute("error", "Không tìm thấy hóa đơn!");
                return "redirect:/student/invoices";
            }

            long amount = invoice.getTotalAmount().longValue();
            String orderInfo = "Thanh toan hoa don " + invoice.getInvoiceNumber();

            String paymentUrl = moMoService.createPaymentUrl(invoiceId, amount, orderInfo);
            if (paymentUrl != null) {
                return "redirect:" + paymentUrl;
            } else {
                ra.addFlashAttribute("error", "Không thể tạo thanh toán MoMo!");
                return "redirect:/payment/checkout/" + invoiceId;
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi tạo thanh toán MoMo: " + e.getMessage());
            return "redirect:/payment/checkout/" + invoiceId;
        }
    }

    /**
     * Callback từ MoMo (redirect URL)
     */
    @GetMapping("/momo/callback")
    public String momoCallback(@RequestParam Map<String, String> params, RedirectAttributes ra) {
        try {
            String resultCode = params.get("resultCode");
            String extraData = params.get("extraData");
            String transactionId = params.get("transId");

            int invoiceId = moMoService.extractInvoiceId(extraData);
            Invoices invoice = invoiceService.findById(invoiceId);

            if ("0".equals(resultCode)) {
                invoiceService.processPayment(invoiceId, invoice.getTotalAmount(), 
                        Payments.PaymentMethod.MOMO, transactionId);
                ra.addFlashAttribute("success", "Thanh toán MoMo thành công!");
            } else {
                ra.addFlashAttribute("error", "Thanh toán MoMo thất bại! Mã lỗi: " + resultCode);
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi xử lý callback: " + e.getMessage());
        }
        return "redirect:/student/invoices";
    }

    /**
     * IPN từ MoMo (server-to-server notification)
     */
    @PostMapping("/momo/ipn")
    @ResponseBody
    public Map<String, Object> momoIPN(@RequestBody Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!moMoService.validateCallback(params)) {
                response.put("resultCode", 1);
                response.put("message", "Invalid signature");
                return response;
            }

            String resultCode = params.get("resultCode");
            String extraData = params.get("extraData");
            String transactionId = params.get("transId");

            if ("0".equals(resultCode)) {
                int invoiceId = moMoService.extractInvoiceId(extraData);
                Invoices invoice = invoiceService.findById(invoiceId);
                if (invoice != null && invoice.getStatus() != Invoices.InvoiceStatus.PAID) {
                    invoiceService.processPayment(invoiceId, invoice.getTotalAmount(), 
                            Payments.PaymentMethod.MOMO, transactionId);
                }
            }

            response.put("resultCode", 0);
            response.put("message", "OK");
        } catch (Exception e) {
            response.put("resultCode", 1);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Tạo thanh toán ZaloPay
     */
    @PostMapping("/zalopay/create")
    public String createZaloPayPayment(@RequestParam int invoiceId, Authentication auth, RedirectAttributes ra) {
        try {
            Invoices invoice = validateInvoiceOwnership(invoiceId, auth);
            if (invoice == null) {
                ra.addFlashAttribute("error", "Không tìm thấy hóa đơn!");
                return "redirect:/student/invoices";
            }

            long amount = invoice.getTotalAmount().longValue();
            String description = "Thanh toan hoa don " + invoice.getInvoiceNumber();

            String paymentUrl = zaloPayService.createPaymentUrl(invoiceId, amount, description);
            if (paymentUrl != null) {
                return "redirect:" + paymentUrl;
            } else {
                ra.addFlashAttribute("error", "Không thể tạo thanh toán ZaloPay!");
                return "redirect:/payment/checkout/" + invoiceId;
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi tạo thanh toán ZaloPay: " + e.getMessage());
            return "redirect:/payment/checkout/" + invoiceId;
        }
    }

    /**
     * Callback từ ZaloPay
     */
    @PostMapping("/zalopay/callback")
    @ResponseBody
    public Map<String, Object> zalopayCallback(@RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            String data = (String) params.get("data");
            String mac = (String) params.get("mac");

            if (!zaloPayService.validateCallback(data, mac)) {
                response.put("return_code", -1);
                response.put("return_message", "Invalid MAC");
                return response;
            }

            // Parse data JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> dataMap = mapper.readValue(data, Map.class);
            
            String appTransId = (String) dataMap.get("app_trans_id");
            int invoiceId = zaloPayService.extractInvoiceId(appTransId);
            
            Invoices invoice = invoiceService.findById(invoiceId);
            if (invoice != null && invoice.getStatus() != Invoices.InvoiceStatus.PAID) {
                String transactionId = String.valueOf(dataMap.get("zp_trans_id"));
                invoiceService.processPayment(invoiceId, invoice.getTotalAmount(), 
                        Payments.PaymentMethod.ZALOPAY, transactionId);
            }

            response.put("return_code", 1);
            response.put("return_message", "success");
        } catch (Exception e) {
            response.put("return_code", 0);
            response.put("return_message", e.getMessage());
        }
        return response;
    }

    /**
     * Redirect sau khi thanh toán ZaloPay
     */
    @GetMapping("/zalopay/result")
    public String zalopayResult(@RequestParam(required = false) String status, RedirectAttributes ra) {
        if ("1".equals(status)) {
            ra.addFlashAttribute("success", "Thanh toán ZaloPay thành công!");
        } else {
            ra.addFlashAttribute("error", "Thanh toán ZaloPay thất bại!");
        }
        return "redirect:/student/invoices";
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
