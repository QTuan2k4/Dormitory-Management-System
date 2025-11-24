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
	// List<Buildings> searchBuildingsByName(String name);
	Optional<Buildings> findBuildingByIdWithRooms(int id);
	Page<Buildings> searchAndFilter(String name, String status, Pageable pageable);
}
