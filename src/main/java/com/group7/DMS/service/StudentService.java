package com.group7.DMS.service;

import com.group7.DMS.entity.Student;
import com.group7.DMS.entity.User;

import java.util.List;

public interface StudentService {
    Student save(Student student);
    Student update(Student student);
    void delete(int id);
    Student findById(int id);
    Student findByStudentId(String studentId);
    Student findByUserId(int userId);
    List<Student> findAll();
    List<Student> findByRegistrationStatus(Student.RegistrationStatus status);
    List<Student> findByFullNameContaining(String name);
    Student createStudent(User user, String fullName, String studentId, String phone, String address);
    void approveRegistration(int studentId);
    void rejectRegistration(int studentId);
    void updateRegistrationStatus(int studentId, Student.RegistrationStatus status);
}
