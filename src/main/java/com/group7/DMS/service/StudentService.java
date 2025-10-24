package com.group7.DMS.service;

import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;

import java.util.List;

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
    Students createStudent(Users user, String fullName, String studentId, String phone, String address);
    void approveRegistration(int studentId);
    void rejectRegistration(int studentId);
    void updateRegistrationStatus(int studentId, Students.RegistrationStatus status);
}
