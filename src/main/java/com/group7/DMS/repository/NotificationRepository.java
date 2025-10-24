package com.group7.DMS.repository;

import com.group7.DMS.entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Integer> {
    List<Notifications> findByUserId(int userId);
    List<Notifications> findByUserIdAndIsRead(int userId, boolean isRead);
    List<Notifications> findByType(Notifications.NotificationType type);
    
    @Query("SELECT n FROM Notifications n WHERE n.user.id = :userId ORDER BY n.sentAt DESC")
    List<Notifications> findByUserIdOrderBySentAtDesc(@Param("userId") int userId);
    
    @Query("SELECT COUNT(n) FROM Notifications n WHERE n.user.id = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") int userId);
}
