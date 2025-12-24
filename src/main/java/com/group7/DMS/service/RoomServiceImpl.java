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
    // Method update for edit room
    @Override
    @Transactional
    public Rooms updateRoomLimited(int roomId, Rooms formRoom) {

        Rooms dbRoom = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));

        dbRoom.setRoomNumber(formRoom.getRoomNumber());
        dbRoom.setSlot(formRoom.getSlot());
        dbRoom.setDescription(formRoom.getDescription());
        dbRoom.setStatus(formRoom.getStatus());

        return roomRepository.save(dbRoom);
    }
    
    @Override
    @Transactional 
    public Rooms saveRoom(Rooms room) {
        int buildingId = room.getBuilding().getId();
        Optional<Buildings> existingBuilding = buildingRepository.findById(buildingId);

        if (!existingBuilding.isPresent()) {
            throw new RuntimeException("Building with ID " + buildingId + " not found.");
        }
        
        if (room.getFloor() > existingBuilding.get().getTotalFloors()) {
             throw new RuntimeException("Floor number " + room.getFloor() + " exceeds total floors of the building.");
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