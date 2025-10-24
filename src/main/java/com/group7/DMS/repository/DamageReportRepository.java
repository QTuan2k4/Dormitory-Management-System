package com.group7.DMS.repository;

import com.group7.DMS.entity.DamageReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DamageReportRepository extends JpaRepository<DamageReports, Long> {
    List<DamageReports> findByStudentId(int studentId);
    List<DamageReports> findByRoomId(int roomId);
    List<DamageReports> findByStatus(DamageReports.DamageStatus status);
    List<DamageReports> findByAssignedStaffId(int staffId);
    
    @Query("SELECT dr FROM DamageReports dr WHERE dr.student.id = :studentId ORDER BY dr.reportedAt DESC")
    List<DamageReports> findByStudentIdOrderByReportedAtDesc(@Param("studentId") int studentId);
    
    @Query("SELECT dr FROM DamageReports dr WHERE dr.room.id = :roomId ORDER BY dr.reportedAt DESC")
    List<DamageReports> findByRoomIdOrderByReportedAtDesc(@Param("roomId") int roomId);
}