package com.group7.DMS.service;

import com.group7.DMS.entity.Buildings;
import com.group7.DMS.repository.BuildingRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Service
public class BuildingServiceImpl implements BuildingService {

	private final BuildingRepository buildingRepository;
	
	@Autowired
    public BuildingServiceImpl(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }
	
	// 1. Thêm/Cập nhật Tòa nhà
    @Override
    public Buildings saveBuilding(Buildings building) {
        return buildingRepository.save(building);
    }
    
 // 2. Lấy tất cả Tòa nhà
    @Override
    public List<Buildings> findAllBuildings() {
        return buildingRepository.findAll();
    }
    
 // 3. Lấy Tòa nhà theo ID
    @Override
    public Optional<Buildings> findBuildingById(int id) {
        return buildingRepository.findById(id);
    }
    
 // 4. Xóa Tòa nhà
    @Override
    public void deleteBuilding(int id) {
        buildingRepository.deleteById(id);
    }
    
 // 5. Tìm kiếm theo tên
    @Override
    public List<Buildings> searchBuildingsByName(String name) {
        return buildingRepository.searchByName(name);
    }
    
    @Override
    @Transactional 
    public Optional<Buildings> findBuildingByIdWithRooms(int id) {
        return buildingRepository.findByIdWithRooms(id);
    }
}
