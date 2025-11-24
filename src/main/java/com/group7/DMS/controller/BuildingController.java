package com.group7.DMS.controller;

import com.group7.DMS.entity.Buildings;
import com.group7.DMS.entity.Rooms;
import com.group7.DMS.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import org.springframework.data.domain.Page; 
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Controller 
@RequestMapping("/admin/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    @Autowired
    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    // --- 1. GET: Hiển thị Danh sách, Tìm kiếm và PHÂN TRANG (View: building-list.html) ---
    @GetMapping({"", "/"}) 
    public String listBuildings(
    		Model model, 
            @RequestParam(defaultValue = "1") int page, 
            @RequestParam(defaultValue = "10") int size, 
    		@RequestParam(required = false) String name,
    		@RequestParam(required = false) String status
    	) {
        
        Pageable pageable = PageRequest.of(page - 1, size);
        
        Page<Buildings> buildingPage = buildingService.searchAndFilter(name, status, pageable);
        
        model.addAttribute("buildingList", buildingPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", buildingPage.getTotalPages());
        model.addAttribute("totalItems", buildingPage.getTotalElements());
        model.addAttribute("pageSize", size); 
        
        model.addAttribute("searchName", name);
        model.addAttribute("searchStatus", status);
        
        return "admin/building-list"; 
    }
    
    // --- 2. GET: Hiển thị Form Thêm mới (View: building-form.html) ---
    @GetMapping("/new")
    public String showNewBuildingForm(Model model) {
        model.addAttribute("building", new Buildings());
        model.addAttribute("pageTitle", "Thêm Tòa Nhà Mới");
        return "admin/building-form"; 
    }
    
    // --- 3. POST: Xử lý Lưu (Thêm mới hoặc Cập nhật) ---
    @PostMapping("/save")
    public String saveBuilding(
    	@Valid	@ModelAttribute("building") Buildings building, 
    	BindingResult bindingResult,
    	RedirectAttributes ra,
    	Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", (building.getId() == 0 ? "Thêm Tòa Nhà Mới" : "Sửa Tòa Nhà (ID: " + building.getId() + ")"));
            return "admin/building-form"; 
        }

        try {
            buildingService.saveBuilding(building);
            ra.addFlashAttribute("message", "Tòa nhà đã được lưu thành công!");
        } catch (IllegalArgumentException e) { 
       
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("building", building); 
            
            model.addAttribute("pageTitle", (building.getId() == 0 ? "Thêm Tòa Nhà Mới" : "Sửa Tòa Nhà (ID: " + building.getId() + ")"));
            return "admin/building-form"; 
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) { 
            
            model.addAttribute("errorMessage", "Lỗi cơ sở dữ liệu: Vui lòng kiểm tra lại thông tin nhập liệu.");
            model.addAttribute("building", building); 
            model.addAttribute("pageTitle", (building.getId() == 0 ? "Thêm Tòa Nhà Mới" : "Sửa Tòa Nhà (ID: " + building.getId() + ")"));
            
            return "admin/building-form"; 
        }

        return "redirect:/admin/buildings";
    }

    // --- 4. GET: Hiển thị Form Sửa (View: building-form.html) ---
    @GetMapping("/edit/{id}")
    public String showEditBuildingForm(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
        try {
            Optional<Buildings> buildingData = buildingService.findBuildingById(id);
            if (buildingData.isPresent()) {
                model.addAttribute("building", buildingData.get());
                model.addAttribute("pageTitle", "Sửa Tòa Nhà (ID: " + id + ")");
                return "admin/building-form";
            } else {
                ra.addFlashAttribute("errorMessage", "Không tìm thấy Tòa nhà ID " + id);
                return "redirect:/admin/buildings";
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi tải dữ liệu.");
            return "redirect:/admin/buildings";
        }
    }

    // --- 5. GET: Xóa Tòa nhà ---
    @GetMapping("/delete/{id}")
    public String deleteBuilding(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            buildingService.deleteBuilding(id);
            ra.addFlashAttribute("message", "Tòa nhà ID " + id + " đã được xóa thành công.");
        } catch (Exception e) {
            // Lỗi xảy ra khi xóa khóa ngoại (ví dụ: Tòa nhà còn phòng)
            ra.addFlashAttribute("errorMessage", "Không thể xóa Tòa nhà ID " + id.toString() + ". Tòa nhà này có thể đang chứa các phòng.");
        }
        return "redirect:/admin/buildings";
    }
    
    // --- 6. GET: Hiển thị Chi tiết Tòa nhà (View: building-details.html) ---
    @GetMapping("/details/{id}")
    public String showBuildingDetails(@PathVariable("id") Integer id, Model model,  RedirectAttributes ra) {
    	try {
    		Optional<Buildings> buildingData = buildingService.findBuildingByIdWithRooms(id);
    		
    		if (buildingData.isPresent()) {
    			Buildings building = buildingData.get();
    			List<Rooms> roomsList = building.getRooms();
    			
    			if (roomsList == null) {
                    roomsList = java.util.Collections.emptyList();
                }
    			
    			model.addAttribute("building", building);
    			model.addAttribute("rooms", roomsList);
    			model.addAttribute("pageTitle", "Chi Tiết Tòa Nhà: " + building.getName());
    			
    			return "admin/building-details";
    		} else {
    			ra.addFlashAttribute("errorMesseage", "Không tìm thấy Tòa nhà ID " + id);
    			return "redirect:/admin/buildings";
    		}
    	} catch (Exception e) {
    		ra.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi tải chi tiết tòa nhà.");
    		return "redirect:/admin/buildings";
    	}
    }
}