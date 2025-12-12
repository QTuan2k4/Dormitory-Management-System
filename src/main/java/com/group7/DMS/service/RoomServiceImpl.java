package com.group7.DMS.service;

import com.group7.DMS.entity.Rooms;
import com.group7.DMS.entity.Buildings; 
import com.group7.DMS.repository.RoomRepository;
import com.group7.DMS.repository.BuildingRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) 
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository; 

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, BuildingRepository buildingRepository) {
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
    @Transactional 
    public Rooms saveRoom(Rooms room) {
        int buildingId = room.getBuilding().getId();
        Optional<Buildings> existingBuilding = buildingRepository.findById(buildingId);

        if (!existingBuilding.isPresent()) {
            throw new RuntimeException("Không tìm thấy tòa nhà với ID " + buildingId);
        }
        
        if (room.getFloor() > existingBuilding.get().getTotalFloors()) {
             throw new RuntimeException("Số tầng " + room.getFloor() + " vượt quá tổng số tầng của tòa nhà.");
        }
        
        // Validate mã phòng trùng trong cùng tòa nhà
        String trimmedRoomNumber = room.getRoomNumber().trim();
        if (room.getId() == 0) {
            // Thêm mới: kiểm tra trùng
            if (roomRepository.existsByBuildingIdAndRoomNumberIgnoreCase(buildingId, trimmedRoomNumber)) {
                throw new RuntimeException("Số phòng '" + trimmedRoomNumber + "' đã tồn tại trong tòa nhà này.");
            }
        } else {
            // Cập nhật: kiểm tra trùng với phòng khác
            if (roomRepository.existsByBuildingIdAndRoomNumberIgnoreCaseAndIdNot(buildingId, trimmedRoomNumber, room.getId())) {
                throw new RuntimeException("Số phòng '" + trimmedRoomNumber + "' đã tồn tại trong tòa nhà này.");
            }
        }
        room.setRoomNumber(trimmedRoomNumber);
        
        // Validate giá thuê
        if (room.getPricePerYear() != null && room.getPricePerYear().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Giá thuê phải là số không âm.");
        }
        
        // Validate diện tích
        if (room.getArea() == null || room.getArea().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Diện tích phải lớn hơn 0.");
        }

        return roomRepository.save(room);
    }

    @Override
    public Optional<Rooms> findRoomById(int id) {
        return roomRepository.findById(id); 
    }

    @Override
    public List<Rooms> findAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteRoom(int id) {
        Optional<Rooms> roomOpt = roomRepository.findById(id);
        if (roomOpt.isPresent()) {
            Rooms room = roomOpt.get();
            // Ngăn xóa phòng đang có sinh viên ở (OCCUPIED hoặc currentOccupants > 0)
            if (room.getStatus() == Rooms.RoomStatus.OCCUPIED || room.getCurrentOccupants() > 0) {
                throw new RuntimeException("Không thể xóa phòng đang có sinh viên ở. Vui lòng chuyển sinh viên sang phòng khác trước.");
            }
        }
        roomRepository.deleteById(id); 
    }

    @Override
    public List<Rooms> findRoomsByBuildingId(int buildingId) {
        return roomRepository.findByBuildingId(buildingId);
    }

    @Override
    public List<Rooms> findRoomsByStatus(Rooms.RoomStatus status) {
        return roomRepository.findByStatus(status);
    }

    @Override
    public Optional<Rooms> findByBuildingAndRoomNumber(int buildingId, String roomNumber) {
        return roomRepository.findByBuildingIdAndRoomNumber(buildingId, roomNumber);
    }

    @Override
    public List<Rooms> findAvailableRooms() {
        return roomRepository.findAvailableRooms();
    }
    
    @Override
    public List<Rooms> findRoomsByBuildingAndFloor(int buildingId, int floor) {
        return roomRepository.findByBuildingIdAndFloor(buildingId, floor);
    }
    
    @Override
    public Map<Integer, List<Rooms>> getRoomsGroupedByFloor(int buildingId) {
        return findRoomsByBuildingId(buildingId)
                .stream()
                .collect(Collectors.groupingBy(Rooms::getFloor));
    }

    // --- TRIỂN KHAI PHƯƠNG THỨC TÌM KIẾM MỚI ---
    
    @Override
    public List<Rooms> searchRoomsByNumber(String roomNumber) {
        return roomRepository.findByRoomNumberContainingIgnoreCase(roomNumber);
    }

    @Override
    public List<Rooms> searchRoomsByNumberAndBuildingId(String roomNumber, int buildingId) {
        return roomRepository.findByRoomNumberContainingIgnoreCaseAndBuildingId(roomNumber, buildingId);
    }
    
    @Override
    public List<Rooms> searchAndFilterRooms(String roomNumber, Integer buildingId, String status,
                                             java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
        Rooms.RoomStatus roomStatus = parseRoomStatus(status);
        
        return roomRepository.searchAndFilter(
            (roomNumber != null && !roomNumber.isEmpty()) ? roomNumber : null,
            buildingId,
            roomStatus,
            minPrice,
            maxPrice
        );
    }
    
    @Override
    public org.springframework.data.domain.Page<Rooms> searchAndFilterRooms(String roomNumber, Integer buildingId, String status,
                                             java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
                                             org.springframework.data.domain.Pageable pageable) {
        Rooms.RoomStatus roomStatus = parseRoomStatus(status);
        
        return roomRepository.searchAndFilterPaged(
            (roomNumber != null && !roomNumber.isEmpty()) ? roomNumber : null,
            buildingId,
            roomStatus,
            minPrice,
            maxPrice,
            pageable
        );
    }
    
    private Rooms.RoomStatus parseRoomStatus(String status) {
        if (status != null && !status.isEmpty()) {
            try {
                return Rooms.RoomStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Bỏ qua nếu status không hợp lệ
            }
        }
        return null;
    }

    // --- Logic Nghiệp vụ Phức tạp ---

    @Override
    @Transactional
    public Rooms updateOccupancy(int roomId, int changeInOccupants) {
        // Đã bỏ ép kiểu (int)
        Optional<Rooms> roomData = roomRepository.findById(roomId);
        
        if (!roomData.isPresent()) {
            throw new RuntimeException("Room with ID " + roomId + " not found.");
        }
        
        Rooms room = roomData.get();
        int newOccupants = room.getCurrentOccupants() + changeInOccupants;
        
        // NGHIỆP VỤ 3: Kiểm tra giới hạn chỗ ở
        if (newOccupants < 0 || newOccupants > room.getSlot()) {
            throw new IllegalArgumentException("Invalid occupancy update. Occupants must be between 0 and slot.");
        }

        room.setCurrentOccupants(newOccupants);
        
        // Cập nhật Status
        if (newOccupants == room.getSlot()) {
            room.setStatus(Rooms.RoomStatus.OCCUPIED);
        } else if (newOccupants > 0 && newOccupants < room.getSlot()) {
            room.setStatus(Rooms.RoomStatus.AVAILABLE); 
        } else if (newOccupants == 0) {
            room.setStatus(Rooms.RoomStatus.AVAILABLE);
        }
        
        return roomRepository.save(room);
    }
    
}