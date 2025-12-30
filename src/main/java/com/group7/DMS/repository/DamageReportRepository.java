package com.group7.DMS.repository;

import com.group7.DMS.entity.DamageReports;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DamageReportRepository extends JpaRepository<DamageReports, Integer> {

	Page<DamageReports> findByStudent_IdOrderByReportedAtDesc(int studentId, Pageable pageable);

	Page<DamageReports> findByRoom_IdOrderByReportedAtDesc(int roomId, Pageable pageable);

	Page<DamageReports> findByStatusOrderByReportedAtDesc(DamageReports.DamageStatus status, Pageable pageable);

	Page<DamageReports> findByAssignedStaff_IdOrderByReportedAtDesc(int staffId, Pageable pageable);

	long countByStatus(DamageReports.DamageStatus status);
}
