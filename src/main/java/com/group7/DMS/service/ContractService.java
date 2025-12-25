package com.group7.DMS.service;

import com.group7.DMS.entity.Contracts;
import java.util.List;

public interface ContractService {
	Contracts save(Contracts contract);

	Contracts update(Contracts contract);

	void delete(int id);

	Contracts findById(int id);

	List<Contracts> findAll();

	List<Contracts> findByStudentId(int studentId);

	List<Contracts> findByRoomId(int roomId);

	List<Contracts> findByStatus(Contracts.ContractStatus status);

	List<Contracts> findActiveContractsByStudent(int studentId);

	List<Contracts> findActiveContractsByRoom(int roomId);

	List<Contracts> findExpiredContracts();
}