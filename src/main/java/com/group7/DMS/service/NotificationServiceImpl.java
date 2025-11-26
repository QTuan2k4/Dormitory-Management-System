package com.group7.DMS.service;

import com.group7.DMS.entity.Notifications;
import com.group7.DMS.entity.Users;
import com.group7.DMS.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Notifications createNotification(Users user, String title, String message, 
                                            Notifications.NotificationType type, 
                                            Notifications.SentVia sentVia) {
        
        Notifications notification = new Notifications();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setSentVia(sentVia);
        notification.setSentAt(LocalDateTime.now());
        notification.setRead(false); 
        
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notifications> getNotificationsByUserId(int userId) {
        // Triển khai logic tìm kiếm theo ID người dùng
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    @Override
    public void markAsRead(int notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}