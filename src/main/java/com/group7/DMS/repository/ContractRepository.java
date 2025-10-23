package com.group7.DMS.repository;

import com.group7.DMS.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {
    List<Contract> findByStudentId(int studentId);
    List<Contract> findByRoomId(int roomId);
    List<Contract> findByStatus(Contract.ContractStatus status);
    
    @Query("SELECT c FROM Contract c WHERE c.student.id = :studentId AND c.status = 'ACTIVE'")
    List<Contract> findActiveContractsByStudent(@Param("studentId") int studentId);
    
    @Query("SELECT c FROM Contract c WHERE c.room.id = :roomId AND c.status = 'ACTIVE'")
    List<Contract> findActiveContractsByRoom(@Param("roomId") int roomId);
    
    @Query("SELECT c FROM Contract c WHERE c.endDate < :date AND c.status = com.group7.DMS.entity.Contract$ContractStatus.ACTIVE")
    List<Contract> findExpiredContracts(@Param("date") LocalDateTime date);
}
