package com.group7.DMS.service;

import com.group7.DMS.entity.Rooms;
import com.group7.DMS.entity.Buildings; // Cần Entity Buildings để kiểm tra
import com.group7.DMS.repository.RoomRepository;
import com.group7.DMS.repository.BuildingRepository; // Nếu cần kiểm tra sự tồn tại của Building
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // Đặt mặc định là chỉ đọc
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository; // Giả sử đã có BuildingRepository

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, BuildingRepository buildingRepository) {
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
    }

    // --- CRUD Cơ bản ---

    @Override
    @Transactional // Cần @Transactional khi thực hiện thay đổi dữ liệu (write operation)
    public Rooms saveRoom(Rooms room) {
        // NGHIỆP VỤ 1: Đảm bảo Building tồn tại trước khi lưu Room
        int buildingId = room.getBuilding().getId();
        Optional<Buildings> existingBuilding = buildingRepository.findById(buildingId);

        if (!existingBuilding.isPresent()) {
            // Ném ngoại lệ tùy chỉnh nếu Building không tồn tại
            throw new RuntimeException("Building with ID " + buildingId + " not found.");
        }
        
        // NGHIỆP VỤ 2: Kiểm tra số tầng hợp lệ (ví dụ: không được vượt quá totalFloors của Building)
        if (room.getFloor() > existingBuilding.get().getTotalFloors()) {
             throw new RuntimeException("Floor number " + room.getFloor() + " exceeds total floors of the building.");
        }

        return roomRepository.save(room);
    }

    // Chú ý: Vì bạn đã định nghĩa ID là int trong entity Buildings/Rooms, 
    // bạn cần đảm bảo Repository của Rooms cũng dùng Integer thay vì Long
    // => Cần sửa lại RoomRepository extends JpaRepository<Rooms, Integer>
    @Override
    public Optional<Rooms> findRoomById(int id) {
        // Cần ép kiểu ID nếu Repository dùng Long
        return roomRepository.findById((int) id); 
    }

    @Override
    public List<Rooms> findAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteRoom(int id) {
        roomRepository.deleteById((int) id); // Cần ép kiểu ID nếu Repository dùng Long
    }

    // --- Nghiệp vụ Tìm kiếm ---

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
        return roomRepository.findByBuildingAndRoomNumber(buildingId, roomNumber);
    }

    @Override
    public List<Rooms> findAvailableRooms() {
        return roomRepository.findAvailableRooms();
    }
    
    @Override
    public List<Rooms> findRoomsByBuildingAndFloor(int buildingId, int floor) {
        return roomRepository.findByBuildingAndFloor(buildingId, floor);
    }

    // --- Logic Nghiệp vụ Phức tạp ---

    @Override
    @Transactional
    public Rooms updateOccupancy(int roomId, int changeInOccupants) {
        Optional<Rooms> roomData = roomRepository.findById((int) roomId);
        
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
            room.setStatus(Rooms.RoomStatus.AVAILABLE); // Hoặc PARTIALLY_OCCUPIED tùy theo logic của bạn
        } else if (newOccupants == 0) {
            room.setStatus(Rooms.RoomStatus.AVAILABLE);
        }
        
        return roomRepository.save(room);
    }
}