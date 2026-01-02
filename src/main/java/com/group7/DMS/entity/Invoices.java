package com.group7.DMS.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Invoices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Version
    private Long version; // Optimistic Locking để tránh xung đột thanh toán

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contracts contract;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Rooms room;

    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Column(name = "electricity_usage")
    private Integer electricityUsage;

    @Column(name = "water_usage")
    private Integer waterUsage;

    private BigDecimal roomFee;
    private BigDecimal electricityFee;
    private BigDecimal waterFee;
    private BigDecimal internetFee;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    public enum InvoiceStatus {
        UNPAID, PAID, OVERDUE
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        if (roomFee != null) total = total.add(roomFee);
        if (electricityFee != null) total = total.add(electricityFee);
        if (waterFee != null) total = total.add(waterFee);
        if (internetFee != null) total = total.add(internetFee);
        return total;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Contracts getContract() { return contract; }
    public void setContract(Contracts contract) { this.contract = contract; }

    public Rooms getRoom() { return room; }
    public void setRoom(Rooms room) { this.room = room; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public Integer getElectricityUsage() { return electricityUsage; }
    public void setElectricityUsage(Integer electricityUsage) { this.electricityUsage = electricityUsage; }

    public Integer getWaterUsage() { return waterUsage; }
    public void setWaterUsage(Integer waterUsage) { this.waterUsage = waterUsage; }

    public BigDecimal getRoomFee() { return roomFee; }
    public void setRoomFee(BigDecimal roomFee) { this.roomFee = roomFee; }

    public BigDecimal getElectricityFee() { return electricityFee; }
    public void setElectricityFee(BigDecimal electricityFee) { this.electricityFee = electricityFee; }

    public BigDecimal getWaterFee() { return waterFee; }
    public void setWaterFee(BigDecimal waterFee) { this.waterFee = waterFee; }

    public BigDecimal getInternetFee() { return internetFee; }
    public void setInternetFee(BigDecimal internetFee) { this.internetFee = internetFee; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
}
