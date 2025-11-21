package com.group7.DMS.service;

import com.group7.DMS.entity.Buildings;

import java.util.List;
import java.util.Optional;

public interface BuildingService {

	Buildings saveBuilding(Buildings building);
	List<Buildings> findAllBuildings();
	Optional<Buildings> findBuildingById(int id);
	void deleteBuilding(int id);
	List<Buildings> searchBuildingsByName(String name);
	
	// ← BỔ SUNG: method bạn đang dùng trong controller
    default List<Buildings> getAllBuildings() { return findAllBuildings(); }
    default Buildings getBuildingById(int id) { return findBuildingById(id).orElse(null); }
	
}
