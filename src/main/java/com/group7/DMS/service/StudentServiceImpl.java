package com.group7.DMS.service;


import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.repository.StudentRepository;
import com.group7.DMS.repository.ContractRepository;
import com.group7.DMS.entity.Contracts;
import com.group7.DMS.entity.Contracts.ContractStatus;
import com.group7.DMS.entity.Rooms;
import com.group7.DMS.entity.Notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired 
    private ContractRepository contractRepository;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired 
    private NotificationService notificationService;
    
    @Autowired
    private MailService mailService;

    @Override
    @Transactional
    public void approveAndAssignRoom(int studentId, int roomId) {

        // 1. Lấy sinh viên
        Students student = findById(studentId);
        if (student == null) {
            throw new RuntimeException("Không tìm thấy sinh viên");
        }

        // 2. Lấy phòng
        Rooms room = roomService.findRoomById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ID: " + roomId));

        // 3. Kiểm tra phòng còn chỗ
        if (room.getCurrentOccupants() >= room.getSlot()) {
            throw new RuntimeException("Phòng " + room.getRoomNumber() + " đã đầy!");
        }

        // 4. Kiểm tra sinh viên đã có hợp đồng ACTIVE chưa
        if (contractRepository.findActiveContractByStudentId(studentId).isPresent()) {
            throw new RuntimeException("Sinh viên này đã được phân phòng rồi!");
        }

        // 5. Chuyển floor (String) → int
        int floorInt = 6;
        try {
            if (student.getFloor() != null && !student.getFloor().trim().isEmpty()) {
                floorInt = Integer.parseInt(student.getFloor().trim());
                if (floorInt < 1 || floorInt > 6) floorInt = 6;
            }
        } catch (Exception e) {
            floorInt = 6;
        }

        // 6. Tính phí theo tầng
        BigDecimal manualFee = switch (floorInt) {
            case 1 -> BigDecimal.valueOf(3_000_000);
            case 2 -> BigDecimal.valueOf(2_700_000);
            case 3 -> BigDecimal.valueOf(2_400_000);
            case 4 -> BigDecimal.valueOf(2_100_000);
            case 5 -> BigDecimal.valueOf(1_800_000);
            case 6 -> BigDecimal.valueOf(1_500_000);
            default -> BigDecimal.valueOf(1_500_000);
        };

        // 7. TẠO HỢP ĐỒNG – ĐÂY LÀ DÒNG BẠN THIẾU!
        Contracts contract = new Contracts();               // <<< QUAN TRỌNG NHẤT
        contract.setStudent(student);
        contract.setRoom(room);
        contract.setStartDate(LocalDateTime.now());
        contract.setEndDate(LocalDateTime.now().plusYears(1));
        contract.setStatus(Contracts.ContractStatus.ACTIVE);
        contract.setManualFee(manualFee);                   // gán phí theo tầng

        // Lưu hợp đồng
        contractRepository.save(contract);

        // 8. Cập nhật số người ở phòng
        roomService.updateOccupancy(roomId, +1);

        // 9. Duyệt hồ sơ sinh viên
        student.setRegistrationStatus(Students.RegistrationStatus.APPROVED);
        studentRepository.save(student); 
        
        // 10. Gửi mial thông báo
        try {
            String to = null;

            if (student.getUser() != null) {
                to = student.getUser().getEmail();
            }

            if (to != null && !to.isBlank()) {
                String subject = "Thông báo duyệt hồ sơ & phân phòng KTX";

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                String buildingName = (room.getBuilding() != null && room.getBuilding().getName() != null)
                        ? room.getBuilding().getName()
                        : "N/A";

                String body =
                        "Xin chào " + (student.getFullName() != null ? student.getFullName() : "bạn") + ",\n\n" +
                        "Hồ sơ đăng ký ký túc xá của bạn đã được DUYỆT.\n" +
                        "Bạn đã được phân phòng như sau:\n" +
                        "- Tòa: " + buildingName + "\n" +
                        "- Phòng: " + room.getRoomNumber() + "\n" +
                        "- Tầng: " + room.getFloor() + "\n" +
                        "- Thời hạn hợp đồng: " + contract.getStartDate().format(fmt) +
                        " đến " + contract.getEndDate().format(fmt) + "\n" +
                        "- Phí (theo tầng): " + manualFee.toPlainString() + " VNĐ\n\n" +
                        "Vui lòng đăng nhập hệ thống để xem chi tiết hợp đồng.\n\n" +
                        "Trân trọng.";

                mailService.send(to, subject, body);
            } else {
                // không có email thì bỏ qua
                System.out.println("[MAIL] Student has no email, skip sending. studentId=" + studentId);
            }
        } catch (Exception ex) {
            // IMPORTANT: không throw để tránh rollback duyệt hồ sơ vì lỗi mail
            ex.printStackTrace();
        }
    }

    @Override
    public Students save(Students student) {
        return studentRepository.save(student);
    }

    @Override
    public Students update(Students student) {
        return studentRepository.save(student);
    }

    @Override
    public void delete(int id) {
        studentRepository.deleteById(id);
    }

    @Override
    public Students findById(int id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Override
    public Students findByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId).orElse(null);
    }

    @Override
    public Students findByUserId(int userId) {
        return studentRepository.findByUserId(userId).orElse(null);
    }

    @Override
    public List<Students> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public List<Students> findByRegistrationStatus(Students.RegistrationStatus status) {
        return studentRepository.findByRegistrationStatus(status);
    }

    @Override
    public List<Students> findByFullNameContaining(String name) {
        return studentRepository.findByFullNameContaining(name);
    }

    @Override
    public Students createStudent(Users user, String fullName, String studentId, String course, String major) {
        Students student = new Students();
        student.setUser(user);
        student.setFullName(fullName);
        student.setStudentId(studentId);
        student.setCourse(course);
        student.setMajor(major);
        student.setApplicationDate(null);
        student.setRegistrationStatus(Students.RegistrationStatus.NOT_SUBMITTED);
        return studentRepository.save(student);
    }

    @Override
    public void approveRegistration(int studentId) {
        Students student = findById(studentId);
        if (student != null) {
            student.setRegistrationStatus(Students.RegistrationStatus.APPROVED);
            studentRepository.save(student);
        }
    }

    @Override
    @Transactional
    public void rejectRegistration(int studentId, String rejectionReason) {
        Students student = findById(studentId);
        if (student != null) {
        	// 1. Cập nhật trạng thái và lưu lý do
            student.setRegistrationStatus(Students.RegistrationStatus.REJECTED);
            student.setRejectionReason(rejectionReason); // <<< LƯU LÝ DO MỚI THÊM
            studentRepository.save(student);
            
         // 2. Gửi thông báo cho sinh viên (Đạt tiêu chí chấp nhận)
            String title = "Hồ sơ đăng ký Ký túc xá bị Từ chối";
            String message = String.format(
                "Hồ sơ đăng ký của bạn đã bị từ chối. Lý do chi tiết: '%s'. Vui lòng kiểm tra lại thông tin cá nhân và giấy tờ đính kèm.",
                rejectionReason
            );
            
            notificationService.createNotification(
                    student.getUser(), 
                    title, 
                    message, 
                    Notifications.NotificationType.APPROVAL_STATUS, 
                    Notifications.SentVia.BOTH
                );
        }
    }

    @Override
    public void updateRegistrationStatus(int studentId, Students.RegistrationStatus status) {
        Students student = findById(studentId);
        if (student != null) {
            student.setRegistrationStatus(status);
            studentRepository.save(student);
        }
    }
    
    @Override
    public Students findByUsername(String username) {
        return studentRepository.findByUsername(username).orElse(null); 
    }
    @Override
    public Optional<Contracts> findActiveContractByUsername(String username) {
        // 1. Tìm đối tượng Student
        Students student = findByUsername(username);
        
        if (student == null) {
            return Optional.empty();
        }

        // 2. Tìm hợp đồng ACTIVE của sinh viên đó
        return contractRepository.findActiveContractByStudentId(student.getId());
    }
    
    @Override
    public List<Students> findRoomMatesByRoomId(int roomId, int currentStudentId) {
        
        // 1. Tìm tất cả các Hợp đồng đang ACTIVE trong phòng đó, loại trừ sinh viên hiện tại
        List<Contracts> roomMateContracts = contractRepository.findActiveContractsForRoomMates(
            roomId, currentStudentId
        );

        // 2. Trích xuất đối tượng Students từ mỗi Contracts và trả về
        return roomMateContracts.stream()
            .map(Contracts::getStudent)
            .collect(Collectors.toList());
    }
   
}