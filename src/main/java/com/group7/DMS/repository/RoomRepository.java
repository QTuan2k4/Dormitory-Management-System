package com.group7.DMS.repository;

import com.group7.DMS.entity.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Rooms, Integer> {
    
    List<Rooms> findByBuildingId(Integer buildingId);

    List<Rooms> findByStatus(Rooms.RoomStatus status);

    List<Rooms> findByFloor(Integer floor);

    List<Rooms> findByRoomNumberContainingIgnoreCase(String roomNumber);

    Optional<Rooms> findByBuildingIdAndRoomNumber(Integer buildingId, String roomNumber);

    List<Rooms> findByBuildingIdAndFloor(Integer buildingId, Integer floor);

    List<Rooms> findByRoomNumberContainingIgnoreCaseAndBuildingId(String roomNumber, Integer buildingId);

    @Query("SELECT r FROM Rooms r WHERE r.currentOccupants < r.slot")
    List<Rooms> findAvailableRooms();
    
    boolean existsByBuildingIdAndRoomNumberIgnoreCase(Integer buildingId, String roomNumber);
    boolean existsByBuildingIdAndRoomNumberIgnoreCaseAndIdNot(Integer buildingId, String roomNumber, Integer id);
    
    // Tìm kiếm và lọc nâng cao (không phân trang)
    @Query("SELECT r FROM Rooms r WHERE " +
           "(:roomNumber IS NULL OR r.roomNumber LIKE %:roomNumber%) AND " +
           "(:buildingId IS NULL OR r.building.id = :buildingId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:minPrice IS NULL OR r.pricePerYear >= :minPrice) AND " +
           "(:maxPrice IS NULL OR r.pricePerYear <= :maxPrice)")
    List<Rooms> searchAndFilter(
        @Param("roomNumber") String roomNumber,
        @Param("buildingId") Integer buildingId,
        @Param("status") Rooms.RoomStatus status,
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice
    );
    
    // Tìm kiếm và lọc nâng cao với phân trang
    @Query("SELECT r FROM Rooms r WHERE " +
           "(:roomNumber IS NULL OR r.roomNumber LIKE %:roomNumber%) AND " +
           "(:buildingId IS NULL OR r.building.id = :buildingId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:minPrice IS NULL OR r.pricePerYear >= :minPrice) AND " +
           "(:maxPrice IS NULL OR r.pricePerYear <= :maxPrice)")
    org.springframework.data.domain.Page<Rooms> searchAndFilterPaged(
        @Param("roomNumber") String roomNumber,
        @Param("buildingId") Integer buildingId,
        @Param("status") Rooms.RoomStatus status,
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice,
        org.springframework.data.domain.Pageable pageable
    );

    // Đếm phòng theo trạng thái
    long countByStatus(Rooms.RoomStatus status);
}