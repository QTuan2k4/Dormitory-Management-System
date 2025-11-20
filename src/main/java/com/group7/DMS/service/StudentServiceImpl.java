package com.group7.DMS.service;


import com.group7.DMS.entity.Students;
import com.group7.DMS.entity.Users;
import com.group7.DMS.repository.StudentRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {
    
    @Autowired
    private StudentRepository studentRepository;

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
        student.setApplicationDate(LocalDate.now());
        student.setRegistrationStatus(Students.RegistrationStatus.PENDING);
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
    public void rejectRegistration(int studentId) {
        Students student = findById(studentId);
        if (student != null) {
            student.setRegistrationStatus(Students.RegistrationStatus.REJECTED);
            studentRepository.save(student);
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
   
}