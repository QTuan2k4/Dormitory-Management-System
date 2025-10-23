package com.group7.DMS.repository;

import com.group7.DMS.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByBuildingId(int buildingId);
    List<Room> findByStatus(Room.RoomStatus status);
    List<Room> findByFloor(int floor);
    
    @Query("SELECT r FROM Room r WHERE r.building.id = :buildingId AND r.roomNumber = :roomNumber")
    Optional<Room> findByBuildingAndRoomNumber(@Param("buildingId") int buildingId, @Param("roomNumber") String roomNumber);
    
    @Query("SELECT r FROM Room r WHERE r.building.id = :buildingId AND r.floor = :floor")
    List<Room> findByBuildingAndFloor(@Param("buildingId") int buildingId, @Param("floor") int floor);
    
    @Query("SELECT r FROM Room r WHERE r.currentOccupants < r.slot")
    List<Room> findAvailableRooms();
}
