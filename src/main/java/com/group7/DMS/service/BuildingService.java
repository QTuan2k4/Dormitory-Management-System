package com.group7.DMS.service;

import com.group7.DMS.entity.Buildings;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BuildingService {

	Buildings saveBuilding(Buildings building);
	List<Buildings> findAllBuildings();
	Optional<Buildings> findBuildingById(int id);
	void deleteBuilding(int id);
	
	Optional<Buildings> findBuildingByIdWithRooms(int id);
	Page<Buildings> searchAndFilter(String name, String status, Pageable pageable);

	List<Buildings> searchBuildingsByName(String name);
	
	// ← BỔ SUNG: method bạn đang dùng trong controller
    default List<Buildings> getAllBuildings() { return findAllBuildings(); }
    default Buildings getBuildingById(int id) { return findBuildingById(id).orElse(null); }
	

}
