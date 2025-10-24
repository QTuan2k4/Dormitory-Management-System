package com.group7.DMS.repository;

import com.group7.DMS.entity.Contracts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contracts, Integer> {
    List<Contracts> findByStudentId(int studentId);
    List<Contracts> findByRoomId(int roomId);
    List<Contracts> findByStatus(Contracts.ContractStatus status);
    
    @Query("SELECT c FROM Contracts c WHERE c.student.id = :studentId AND c.status = com.group7.DMS.entity.Contracts$ContractStatus.ACTIVE")
    List<Contracts> findActiveContractsByStudent(@Param("studentId") int studentId);
    
    @Query("SELECT c FROM Contracts c WHERE c.room.id = :roomId AND c.status = com.group7.DMS.entity.Contracts$ContractStatus.ACTIVE")
    List<Contracts> findActiveContractsByRoom(@Param("roomId") int roomId);
    
    @Query("SELECT c FROM Contracts c WHERE c.endDate < :date AND c.status = com.group7.DMS.entity.Contracts$ContractStatus.ACTIVE")
    List<Contracts> findExpiredContracts(@Param("date") LocalDateTime date);
}