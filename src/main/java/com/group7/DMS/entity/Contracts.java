package com.group7.DMS.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
public class Contracts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Students student;

    @ManyToOne
    private Rooms room;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ContractStatus status;
    @Column(name = "manual_fee", precision = 12, scale = 2, nullable = false)
    private BigDecimal manualFee;

    public BigDecimal getManualFee() {
        return manualFee;
    }

    public void setManualFee(BigDecimal manualFee) {
        this.manualFee = manualFee;
    }

    public enum ContractStatus {
        ACTIVE, INACTIVE, TERMINATED
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Students getStudent() {
        return student;
    }

    public void setStudent(Students student) {
        this.student = student;
    }

    public Rooms getRoom() {
        return room;
    }

    public void setRoom(Rooms room) {
        this.room = room;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }
}