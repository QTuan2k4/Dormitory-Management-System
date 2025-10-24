package com.group7.DMS.repository;

import com.group7.DMS.entity.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Students, Integer> {
    Optional<Students> findByStudentId(String studentId);
    Optional<Students> findByUserId(int userId);
    List<Students> findByRegistrationStatus(Students.RegistrationStatus status);
    
    @Query("SELECT s FROM Students s WHERE s.fullName LIKE %:name%")
    List<Students> findByFullNameContaining(@Param("name") String name);
}
