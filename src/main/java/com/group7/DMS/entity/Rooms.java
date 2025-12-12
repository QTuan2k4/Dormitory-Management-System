package com.group7.DMS.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rooms", uniqueConstraints = { 
	    @UniqueConstraint(columnNames = {"building_id", "room_number"}, name = "UK_room_in_building")
	})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rooms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "building_id", nullable = false)
    @NotNull(message = "Tòa nhà không được để trống")
    private Buildings building;

    @NotBlank(message = "Số phòng không được để trống")
    @Size(min = 1, max = 20, message = "Số phòng phải từ 1 đến 20 ký tự")
    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @NotNull(message = "Số tầng không được để trống")
    @Min(value = 1, message = "Số tầng phải lớn hơn 0")
    @Column(nullable = false)
    private int floor;

    @NotNull(message = "Số chỗ không được để trống")
    @Min(value = 1, message = "Số chỗ phải lớn hơn 0")
    @Column
    private int slot = 4;

    @Column(name = "current_occupants")
    private int currentOccupants = 0;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Trạng thái phòng không được để trống")
    private RoomStatus status = RoomStatus.AVAILABLE;

    @NotNull(message = "Giá thuê không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá thuê phải là số không âm")
    @Column(name = "price_per_year", precision = 10, scale = 2)
    private BigDecimal pricePerYear = BigDecimal.ZERO;
    
    @NotNull(message = "Diện tích không được để trống")
    @DecimalMin(value = "0.1", inclusive = true, message = "Diện tích phải lớn hơn 0")
    @Column(name = "area", precision = 10, scale = 2)
    private BigDecimal area;

    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Buildings getBuilding() {
		return building;
	}

	public void setBuilding(Buildings building) {
		this.building = building;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public int getCurrentOccupants() {
		return currentOccupants;
	}

	public void setCurrentOccupants(int currentOccupants) {
		this.currentOccupants = currentOccupants;
	}

	public RoomStatus getStatus() {
		return status;
	}

	public void setStatus(RoomStatus status) {
		this.status = status;
	}

	public BigDecimal getPricePerYear() {
		return pricePerYear;
	}

	public void setPricePerYear(BigDecimal pricePerYear) {
		this.pricePerYear = pricePerYear;
	}
	
	public BigDecimal getArea() {
		return area;
	}

	public void setArea(BigDecimal area) {
		this.area = area;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<Contracts> getContracts() {
		return contracts;
	}

	public void setContracts(List<Contracts> contracts) {
		this.contracts = contracts;
	}

	public List<DamageReports> getDamageReports() {
		return damageReports;
	}

	public void setDamageReports(List<DamageReports> damageReports) {
		this.damageReports = damageReports;
	}

	@Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contracts> contracts;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DamageReports> damageReports;

    public enum RoomStatus {
    	AVAILABLE, OCCUPIED, MAINTENANCE
    }
}
