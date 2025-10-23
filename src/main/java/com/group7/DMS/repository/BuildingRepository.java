package com.group7.DMS.repository;

import com.group7.DMS.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Integer> {
    List<Building> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT b FROM Building b WHERE b.name LIKE %:name%")
    List<Building> searchByName(@Param("name") String name);
}
