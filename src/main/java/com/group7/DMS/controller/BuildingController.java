package com.group7.DMS.controller;

import com.group7.DMS.entity.Buildings;
import com.group7.DMS.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // --- 1. GET: Hiển thị Danh sách và Tìm kiếm (View: building-list.html) ---
    @GetMapping({"", "/"}) 
    public String listBuildings(Model model, @RequestParam(required = false) String name) {
        
        List<Buildings> buildings;
        if (name != null && !name.isEmpty()) {
            buildings = buildingService.searchBuildingsByName(name);
        } else {
            buildings = buildingService.findAllBuildings();
        }
        
        model.addAttribute("buildingList", buildings);
        model.addAttribute("searchName", name); 
        
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
    public String saveBuilding(@ModelAttribute("building") Buildings building, RedirectAttributes ra) {
        buildingService.saveBuilding(building);
        ra.addFlashAttribute("message", "Tòa nhà đã được lưu thành công!");
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
            ra.addFlashAttribute("errorMessage", "Không thể xóa Tòa nhà ID " + id.toString() + ". Tòa nhà này có thể đang chứa các phòng.");
        }
        return "redirect:/admin/buildings";
    }
}