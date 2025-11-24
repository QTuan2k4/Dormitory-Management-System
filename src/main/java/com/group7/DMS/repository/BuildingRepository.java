package com.group7.DMS.repository;

import com.group7.DMS.entity.Buildings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Buildings, Integer> {
    List<Buildings> findByNameContainingIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, int id);
    
//    @Query("SELECT b FROM Buildings b WHERE b.name LIKE %:name%")
    List<Buildings> searchByName(@Param("name") String name);
    
    @Query("SELECT b FROM Buildings b LEFT JOIN FETCH b.rooms r WHERE b.id = :id")
    Optional<Buildings> findByIdWithRooms(@Param("id") int id);
    
    @Query("SELECT b FROM Buildings b " +
            "WHERE (:name IS NULL OR :name = '' OR b.name LIKE %:name%) " +
            "AND (:status IS NULL OR :status = '' OR b.status = :status)")
     List<Buildings> searchAndFilter(@Param("name") String name, @Param("status") String status);
    
    Page<Buildings> findAll(Pageable pageable);
    @Query("SELECT b FROM Buildings b WHERE "
            + "(:name IS NULL OR b.name LIKE %:name%) AND "
            + "(:status IS NULL OR b.status = :status)")
     Page<Buildings> searchAndFilter(
         @Param("name") String name,
         @Param("status") String status,
         Pageable pageable);
    
}
