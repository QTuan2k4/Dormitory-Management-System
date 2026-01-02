package com.group7.DMS.service;

import com.group7.DMS.entity.Rooms;
import java.util.List;
import java.util.Optional;
import java.util.Map;

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
    
    // Tìm kiếm và lọc nâng cao (không phân trang - giữ lại cho tương thích)
    List<Rooms> searchAndFilterRooms(String roomNumber, Integer buildingId, String status, 
                                      java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
    
    // Tìm kiếm và lọc nâng cao với phân trang
    org.springframework.data.domain.Page<Rooms> searchAndFilterRooms(String roomNumber, Integer buildingId, String status, 
                                      java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
                                      org.springframework.data.domain.Pageable pageable);

    Rooms updateOccupancy(int roomId, int changeInOccupants);
 // ← BỔ SUNG: gom nhóm theo tầng để hiển thị đẹp
    Map<Integer, List<Rooms>> getRoomsGroupedByFloor(int buildingId);
    Rooms updateRoomLimited(int roomId, Rooms room);
    
    // Thống kê
    long countAllRooms();
    long countOccupiedRooms();
}