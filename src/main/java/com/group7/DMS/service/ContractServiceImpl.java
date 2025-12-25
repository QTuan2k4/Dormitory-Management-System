package com.group7.DMS.service;

import com.group7.DMS.entity.Contracts;
import com.group7.DMS.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {

	@Autowired
	private ContractRepository contractRepository;

	@Override
	public Contracts save(Contracts contract) {
		return contractRepository.save(contract);
	}

	@Override
	public Contracts update(Contracts contract) {
		return contractRepository.save(contract);
	}

	@Override
	public void delete(int id) {
		contractRepository.deleteById(id);
	}

	@Override
	public Contracts findById(int id) {
		return contractRepository.findById(id).orElse(null);
	}

	@Override
	public List<Contracts> findAll() {
		return contractRepository.findAll();
	}

	@Override
	public List<Contracts> findByStudentId(int studentId) {
		return contractRepository.findByStudentId(studentId);
	}

	@Override
	public List<Contracts> findByRoomId(int roomId) {
		return contractRepository.findByRoomId(roomId);
	}

	@Override
	public List<Contracts> findByStatus(Contracts.ContractStatus status) {
		return contractRepository.findByStatus(status);
	}

	@Override
	public List<Contracts> findActiveContractsByStudent(int studentId) {
		return contractRepository.findActiveContractsByStudent(studentId);
	}

	@Override
	public List<Contracts> findActiveContractsByRoom(int roomId) {
		return contractRepository.findActiveContractsByRoom(roomId);
	}

	@Override
	public List<Contracts> findExpiredContracts() {
		return contractRepository.findExpiredContracts(LocalDateTime.now());
	}
}