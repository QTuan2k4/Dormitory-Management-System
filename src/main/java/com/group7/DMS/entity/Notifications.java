package com.group7.DMS.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "sent_via")
    private SentVia sentVia = SentVia.EMAIL;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    public enum NotificationType {
        REMINDER_PAYMENT, APPROVAL_STATUS, DAMAGE_UPDATE, GENERAL
    }

    public enum SentVia {
        EMAIL, APP, BOTH
    }
}
