package com.group7.DMS.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "buildings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Buildings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Tên tòa nhà không được để trống") 
    @Size(min = 2, max = 100, message = "Tên tòa nhà phải từ 2 đến 100 ký tự")
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @NotNull(message = "Số tầng không được để trống")
    @Min(value = 0, message = "Số tầng phải là số dương")
    @Column(name = "total_floors")
    private int totalFloors = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Trạng thái không được để trống")
    @Column(name = "status", length = 50, nullable = false)
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Rooms> rooms;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
