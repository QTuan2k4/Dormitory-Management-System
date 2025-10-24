package com.group7.DMS.repository;

import com.group7.DMS.entity.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Rooms, Long> {
    List<Rooms> findByBuildingId(int buildingId);
    List<Rooms> findByStatus(Rooms.RoomStatus status);
    List<Rooms> findByFloor(int floor);
    
    @Query("SELECT r FROM Rooms r WHERE r.building.id = :buildingId AND r.roomNumber = :roomNumber")
    Optional<Rooms> findByBuildingAndRoomNumber(@Param("buildingId") int buildingId, @Param("roomNumber") String roomNumber);
    
    @Query("SELECT r FROM Rooms r WHERE r.building.id = :buildingId AND r.floor = :floor")
    List<Rooms> findByBuildingAndFloor(@Param("buildingId") int buildingId, @Param("floor") int floor);
    
    @Query("SELECT r FROM Rooms r WHERE r.currentOccupants < r.slot")
    List<Rooms> findAvailableRooms();
}