package com.group7.DMS.repository;

import com.group7.DMS.entity.DamageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DamageReportRepository extends JpaRepository<DamageReport, Integer> {
    List<DamageReport> findByStudentId(int studentId);
    List<DamageReport> findByRoomId(int roomId);
    List<DamageReport> findByStatus(DamageReport.DamageStatus status);
    List<DamageReport> findByAssignedStaffId(int staffId);
    
    @Query("SELECT dr FROM DamageReport dr WHERE dr.student.id = :studentId ORDER BY dr.reportedAt DESC")
    List<DamageReport> findByStudentIdOrderByReportedAtDesc(@Param("studentId") int studentId);
    
    @Query("SELECT dr FROM DamageReport dr WHERE dr.room.id = :roomId ORDER BY dr.reportedAt DESC")
    List<DamageReport> findByRoomIdOrderByReportedAtDesc(@Param("roomId") int roomId);
}
