package com.group7.DMS.service;

import com.group7.DMS.entity.Buildings;
import com.group7.DMS.repository.BuildingRepository;
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
        // Sử dụng Optional để xử lý trường hợp không tìm thấy
        return buildingRepository.findById(id);
    }
    
 // 4. Xóa Tòa nhà
    @Override
    public void deleteBuilding(int id) {
        // Có thể thêm kiểm tra xem Tòa nhà có phòng đang có sinh viên ở không trước khi xóa
        buildingRepository.deleteById(id);
    }
    
 // 5. Tìm kiếm theo tên
    @Override
    public List<Buildings> searchBuildingsByName(String name) {
        // Bạn có thể chọn 1 trong 2 phương thức Repository đã tạo:
        // return buildingRepository.findByNameContainingIgnoreCase(name);
        return buildingRepository.searchByName(name);
    }
}
