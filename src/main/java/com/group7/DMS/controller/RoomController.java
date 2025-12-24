package com.group7.DMS.controller;

import com.group7.DMS.entity.Buildings;
import com.group7.DMS.entity.Rooms;
import com.group7.DMS.service.RoomService;
import com.group7.DMS.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
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

    // --- 1. GET: Hiển thị Danh sách Phòng với Tìm kiếm, Lọc và Phân trang ---
    @GetMapping
    public String listRooms(Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String roomNumber,
        @RequestParam(required = false) Integer buildingId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice) { 
        
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // Sử dụng phương thức tìm kiếm nâng cao với phân trang
        Page<Rooms> roomPage = roomService.searchAndFilterRooms(roomNumber, buildingId, status, minPrice, maxPrice, pageable);

        model.addAttribute("roomList", roomPage.getContent());
        model.addAttribute("buildingList", buildingService.findAllBuildings());
        
        // Thông tin phân trang
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomPage.getTotalPages());
        model.addAttribute("totalItems", roomPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        // Giữ lại giá trị tìm kiếm để hiển thị trên form
        model.addAttribute("searchRoomNumber", roomNumber);
        model.addAttribute("searchBuildingId", buildingId);
        model.addAttribute("searchStatus", status);
        model.addAttribute("searchMinPrice", minPrice);
        model.addAttribute("searchMaxPrice", maxPrice);

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
        
        return "admin/room-form-edit";
    }

    // --- 4. POST: Xử lý Lưu (Thêm mới hoặc Cập nhật) ---
    // URL: POST /admin/rooms/save
    @PostMapping("/save")
    public String saveRoom(@ModelAttribute("room") Rooms room, RedirectAttributes ra) {
        try {
            if (room.getId() == 0) {
                roomService.saveRoom(room);
                ra.addFlashAttribute("message", "Thêm phòng thành công!");
            } else {
                // Giới hạn field
                roomService.updateRoomLimited(room.getId(), room);
                ra.addFlashAttribute("message", "Cập nhật phòng thành công!");
            }
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return room.getId() == 0
                    ? "redirect:/admin/rooms/new"
                    : "redirect:/admin/rooms/edit/" + room.getId();
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