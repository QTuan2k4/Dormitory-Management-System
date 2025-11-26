package com.group7.DMS.service;

import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.entity.Contracts;

import java.util.List;
import java.util.Optional;



public interface StudentService {
    Students save(Students student);
    Students update(Students student);
    void delete(int id);
    Students findById(int id);
    Students findByStudentId(String studentId);
    Students findByUserId(int userId);
    List<Students> findAll();
    List<Students> findByRegistrationStatus(Students.RegistrationStatus status);
    List<Students> findByFullNameContaining(String name);
    // FIX: Cập nhật phương thức để nhận course và major từ form đăng ký
    Students createStudent(Users user, String fullName, String studentId, String course, String major); 
    void approveRegistration(int studentId);
    void rejectRegistration(int studentId, String rejectionReason); //THÊM tham số String rejectionReason
    void updateRegistrationStatus(int studentId, Students.RegistrationStatus status);
    Students findByUsername(String username);
    Optional<Contracts> findActiveContractByUsername(String username);
    List<Students> findRoomMatesByRoomId(int roomId, int currentStudentId);
    void approveAndAssignRoom(int studentId, int roomId);

}