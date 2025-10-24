package com.group7.DMS.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "damage_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DamageReports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Students student;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Rooms room;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "photo_paths", columnDefinition = "LONGTEXT")
    private String photoPaths;

    @Enumerated(EnumType.STRING)
    private DamageStatus status = DamageStatus.REPORTED;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private Users assignedStaff;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public enum DamageStatus {
        REPORTED, ASSIGNED, RESOLVED, REJECTED
    }
}
