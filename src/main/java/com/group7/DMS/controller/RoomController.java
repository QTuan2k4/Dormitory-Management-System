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
@RequestMapping("/admin/rooms") // Sử dụng /admin/rooms để đồng bộ với cấu trúc của bạn
public class RoomController {

    private final RoomService roomService;
    private final BuildingService buildingService; // Dùng để lấy danh sách tòa nhà

    @Autowired
    public RoomController(RoomService roomService, BuildingService buildingService) {
        this.roomService = roomService;
        this.buildingService = buildingService;
    }

    // --- 1. GET: Hiển thị Danh sách Phòng ---
    // URL: /admin/rooms
    @GetMapping
    public String listRooms(Model model) {
        
        List<Rooms> rooms = roomService.findAllRooms();
        
        model.addAttribute("roomList", rooms);
        // Thêm danh sách Buildings để tiện cho việc lọc/hiển thị tên Building
        model.addAttribute("buildingList", buildingService.findAllBuildings()); 
        
        // Trả về view: src/main/resources/templates/admin/room-list.html
        return "admin/room-list"; 
    }

    // --- 2. GET: Hiển thị Form Thêm mới ---
    // URL: /admin/rooms/new
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
    // URL: /admin/rooms/edit/{id}
    @GetMapping("/edit/{id}")
    public String showEditRoomForm(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
    	Optional<Rooms> roomOpt = roomService.findRoomById(id);
        if (roomOpt.isEmpty()) {
             // Xử lý lỗi không tìm thấy
             return "redirect:/admin/rooms"; 
        }

        Rooms room = roomOpt.get();
        // Vẫn nên kiểm tra nếu building có thể bị null do lỗi dữ liệu
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
            // Spring sẽ tự động bind building_id từ form vào trường Buildings
            roomService.saveRoom(room);
            ra.addFlashAttribute("message", "Phòng đã được lưu thành công!");
        } catch (RuntimeException e) {
             // Xử lý các ngoại lệ từ Service (như Building không tồn tại, số tầng không hợp lệ)
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            // Có thể chuyển hướng lại form để người dùng sửa
            if (room.getId() == 0) {
                 return "redirect:/admin/rooms/new";
            } else {
                 return "redirect:/admin/rooms/edit/" + room.getId();
            }
        }
        return "redirect:/admin/rooms"; 
    }
    
    // --- 5. GET: Xóa Phòng ---
    // URL: /admin/rooms/delete/{id}
    @GetMapping("/rooms/delete/{id}")
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