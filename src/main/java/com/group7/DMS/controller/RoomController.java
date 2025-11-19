package com.group7.DMS.controller;

import com.group7.DMS.entity.Buildings;
import com.group7.DMS.entity.Rooms;
import com.group7.DMS.service.RoomService;
import com.group7.DMS.service.BuildingService; // Cần lấy danh sách Buildings cho Dropdown
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/rooms") 
public class RoomController {

    private final RoomService roomService;
    private final BuildingService buildingService; 

    @Autowired
    public RoomController(RoomService roomService, BuildingService buildingService) {
        this.roomService = roomService;
        this.buildingService = buildingService;
    }

    // --- 1. GET: Hiển thị Danh sách Phòng ---
    @GetMapping
    public String listRooms(Model model,
        @RequestParam(required = false) String roomNumber,
        @RequestParam(required = false) Integer buildingId) { 
        
        List<Rooms> rooms;
        
        // --- LOGIC XỬ LÝ TÌM KIẾM ---
        if (roomNumber != null && !roomNumber.isEmpty() && buildingId != null) {
            rooms = roomService.searchRoomsByNumberAndBuildingId(roomNumber, buildingId);
            model.addAttribute("searchBuildingId", buildingId);

        } else if (roomNumber != null && !roomNumber.isEmpty()) {
            rooms = roomService.searchRoomsByNumber(roomNumber);
        } else if (buildingId != null) {
            rooms = roomService.findRoomsByBuildingId(buildingId);
            model.addAttribute("searchBuildingId", buildingId);    
        } else {
            rooms = roomService.findAllRooms();
        }

        model.addAttribute("roomList", rooms);
        model.addAttribute("buildingList", buildingService.findAllBuildings());
        model.addAttribute("searchRoomNumber", roomNumber);

        return "admin/room-list";
    }

    // --- 2. GET: Hiển thị Form Thêm mới ---
    @GetMapping("/new")
    public String showNewRoomForm(Model model) {
    	Rooms room = new Rooms();
    	if (room.getBuilding() == null) {
            room.setBuilding(new Buildings());
        }
        
        model.addAttribute("room", room);
        model.addAttribute("pageTitle", "Thêm Phòng Mới");
        model.addAttribute("buildingList", buildingService.findAllBuildings()); 
        return "admin/room-form";
    }
    
    // --- 3. GET: Hiển thị Form Sửa ---
    @GetMapping("/edit/{id}")
    public String showEditRoomForm(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
    	Optional<Rooms> roomOpt = roomService.findRoomById(id);
        if (roomOpt.isEmpty()) {
             return "redirect:/admin/rooms"; 
        }
        Rooms room = roomOpt.get();
        if (room.getBuilding() == null) {
             room.setBuilding(new Buildings());
        }
        
        model.addAttribute("room", room);
        model.addAttribute("pageTitle", "Chỉnh Sửa Phòng: " + room.getRoomNumber());
        model.addAttribute("buildingList", buildingService.findAllBuildings());
        
        return "admin/room-form";
    }

    // --- 4. POST: Xử lý Lưu (Thêm mới hoặc Cập nhật) ---
    // URL: POST /admin/rooms/save
    @PostMapping("/save")
    public String saveRoom(@ModelAttribute("room") Rooms room, RedirectAttributes ra) {
        try {
            roomService.saveRoom(room);
            ra.addFlashAttribute("message", "Phòng đã được lưu thành công!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            if (room.getId() == 0) {
                 return "redirect:/admin/rooms/new";
            } else {
                 return "redirect:/admin/rooms/edit/" + room.getId();
            }
        }
        return "redirect:/admin/rooms"; 
    }
    
    // --- 5. GET: Xóa Phòng ---
    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            roomService.deleteRoom(id);
            ra.addFlashAttribute("message", "Phòng ID " + id + " đã được xóa thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa Phòng ID " + id.toString() + ". Có thể phòng này đang có hợp đồng liên quan.");
        }
        return "redirect:/admin/rooms";
    }
}