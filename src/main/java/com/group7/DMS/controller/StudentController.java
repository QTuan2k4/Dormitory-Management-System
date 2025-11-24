package com.group7.DMS.controller;

import com.group7.DMS.entity.Invoices;
import com.group7.DMS.entity.Payments;
import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.entity.Rooms;
import com.group7.DMS.entity.Contracts;
import com.group7.DMS.service.InvoiceService;
import com.group7.DMS.service.StudentService;
import com.group7.DMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

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
        	
        	studentService.update(currentStudent);
        		
        }
        return "redirect:/student/personal-info";
    }

//    @GetMapping("/room-info")
//    public String roomInfo(Model model, Authentication auth) {
//        String username = auth.getName();
//        Users user = userService.findByUsername(username);
//        Students student = studentService.findByUserId(user.getId());
//        
//        model.addAttribute("user", user);
//        model.addAttribute("student", student);
//        return "student/room-info";
//    }
    
    @GetMapping("/room-info")
    public String roomInfo(Model model, Authentication auth) {
        
        // 1. Lấy đối tượng Students hiện tại
        Students student = getStudentFromAuth(auth);
        if (student == null) {
            return "redirect:/login";
        }
        
        // 2. Lấy Hợp đồng đang hoạt động (ACTIVE)
        Optional<Contracts> activeContractOpt = studentService.findActiveContractByUsername(auth.getName());

        if (activeContractOpt.isEmpty()) {
            // Trường hợp 1: Sinh viên không có hợp đồng ACTIVE
            model.addAttribute("hasRoom", false);
        } else {
            // Trường hợp 2: Có phòng
            Contracts activeContract = activeContractOpt.get();
            Rooms room = activeContract.getRoom(); 
            
            // 3. Lấy danh sách bạn cùng phòng
            List<Students> roomMates = studentService.findRoomMatesByRoomId(room.getId(), student.getId());
            
            // 4. Truyền dữ liệu chi tiết phòng
            model.addAttribute("hasRoom", true); 
            model.addAttribute("room", room);
            // Giả sử Rooms Entity có quan hệ Building
            model.addAttribute("building", room.getBuilding()); 
            model.addAttribute("roomMates", roomMates);
            model.addAttribute("contract", activeContract); // Có thể cần hiển thị thêm ngày bắt đầu/kết thúc
        }
        
        // Luôn truyền các đối tượng cơ bản
        model.addAttribute("user", student.getUser());
        model.addAttribute("student", student);
        
        return "student/room-info";
    }
    

    @GetMapping("/invoices")
    public String invoiceList(Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());
        
        int studentId = student.getId();

        List<Invoices> invoices = invoiceService.findByStudentId(studentId);

        BigDecimal totalPaid = invoiceService.calculateTotalPaidAmount(studentId);
        BigDecimal totalUnpaid = invoiceService.calculateTotalUnpaidAmount(studentId);
        List<Payments> recentPayments = invoiceService.findRecentPaymentsByStudent(studentId, 5); // Lấy 5 giao dịch gần nhất
        
        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("invoices", invoices);
        
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalUnpaid", totalUnpaid);
        model.addAttribute("recentPayments", recentPayments);
        
        return "student/invoice-list";
    }

    @GetMapping("/invoices/{id}/payment")
    public String paymentPage(@PathVariable int id, Model model, Authentication auth) {
        String username = auth.getName();
        Users user = userService.findByUsername(username);
        Students student = studentService.findByUserId(user.getId());
        
        Invoices invoice = invoiceService.findById(id);

        if (invoice == null) {
            return "redirect:/student/invoices?error=invoice_not_found";
        }

        if (invoice.getContract().getStudent().getId() != student.getId()) {
            return "redirect:/student/invoices?error=unauthorized";
        }

        if (invoice.getStatus() == Invoices.InvoiceStatus.PAID) {
            return "redirect:/student/invoices?error=already_paid";
        }

        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("invoice", invoice);
        return "student/payment";
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
    
    private Students getStudentFromAuth(Authentication auth) {
        String username = auth.getName();
        return studentService.findByUsername(username); 
    }
 
   
}
