package com.group7.DMS.service;

import com.group7.DMS.entity.Student;
import com.group7.DMS.entity.User;
import com.group7.DMS.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {
    
    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Student save(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public Student update(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public void delete(int id) {
        studentRepository.deleteById(id);
    }

    @Override
    public Student findById(int id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Override
    public Student findByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId).orElse(null);
    }

    @Override
    public Student findByUserId(int userId) {
        return studentRepository.findByUserId(userId).orElse(null);
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public List<Student> findByRegistrationStatus(Student.RegistrationStatus status) {
        return studentRepository.findByRegistrationStatus(status);
    }

    @Override
    public List<Student> findByFullNameContaining(String name) {
        return studentRepository.findByFullNameContaining(name);
    }

    @Override
    public Student createStudent(User user, String fullName, String studentId, String phone, String address) {
        Student student = new Student();
        student.setUser(user);
        student.setFullName(fullName);
        student.setStudentId(studentId);
        student.setPhone(phone);
        student.setAddress(address);
        student.setRegistrationStatus(Student.RegistrationStatus.PENDING);
        return studentRepository.save(student);
    }

    @Override
    public void approveRegistration(int studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            student.setRegistrationStatus(Student.RegistrationStatus.APPROVED);
            studentRepository.save(student);
        }
    }

    @Override
    public void rejectRegistration(int studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            student.setRegistrationStatus(Student.RegistrationStatus.REJECTED);
            studentRepository.save(student);
        }
    }

    @Override
    public void updateRegistrationStatus(int studentId, Student.RegistrationStatus status) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            student.setRegistrationStatus(status);
            studentRepository.save(student);
        }
    }
}
