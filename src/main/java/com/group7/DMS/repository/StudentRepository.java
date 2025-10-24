package com.group7.DMS.repository;

import com.group7.DMS.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByUserId(int userId);
    List<Student> findByRegistrationStatus(Student.RegistrationStatus status);
    
    @Query("SELECT s FROM Student s WHERE s.fullName LIKE %:name%")
    List<Student> findByFullNameContaining(@Param("name") String name);
}
