package com.group7.DMS.repository;

import com.group7.DMS.entity.Buildings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Buildings, Integer> {
    List<Buildings> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT b FROM Buildings b WHERE b.name LIKE %:name%")
    List<Buildings> searchByName(@Param("name") String name);
    
    @Query("SELECT b FROM Buildings b LEFT JOIN FETCH b.rooms r WHERE b.id = :id")
    Optional<Buildings> findByIdWithRooms(@Param("id") int id);
}
