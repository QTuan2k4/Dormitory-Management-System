package com.group7.DMS.service;

import com.group7.DMS.entity.Rooms;
import java.util.List;
import java.util.Optional;

public interface RoomService {
    Rooms saveRoom(Rooms room);
    Optional<Rooms> findRoomById(int id); 
    List<Rooms> findAllRooms();
    void deleteRoom(int id);

    List<Rooms> findRoomsByBuildingId(int buildingId);
    List<Rooms> findRoomsByStatus(Rooms.RoomStatus status);
    Optional<Rooms> findByBuildingAndRoomNumber(int buildingId, String roomNumber);
    List<Rooms> findAvailableRooms();
    List<Rooms> findRoomsByBuildingAndFloor(int buildingId, int floor);

    List<Rooms> searchRoomsByNumber(String roomNumber);
    List<Rooms> searchRoomsByNumberAndBuildingId(String roomNumber, int buildingId);

    Rooms updateOccupancy(int roomId, int changeInOccupants);
}