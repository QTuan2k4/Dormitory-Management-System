package com.group7.DMS.service;

import com.group7.DMS.entity.Notifications;
import com.group7.DMS.entity.Users;

import java.util.List;

public interface NotificationService {
	
	Notifications createNotification(Users user, String title, String message, 
            Notifications.NotificationType type, 
            Notifications.SentVia sentVia);

	List<Notifications> getNotificationsByUserId(int userId);
	
	void markAsRead(int notificationId);
}
