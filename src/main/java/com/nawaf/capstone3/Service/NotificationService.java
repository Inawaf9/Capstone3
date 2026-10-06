package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.Notification;
import com.nawaf.capstone3.Model.User;
import com.nawaf.capstone3.Repository.NotificationRepository;
import com.nawaf.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<Notification> getNotifications() {
        return notificationRepository.findAll();
    }

    public Notification getNotificationById(Integer id) {
        Notification notification = notificationRepository.findNotificationById(id);

        if (notification == null) throw new ApiException("Notification not found");

        return notification;
    }

    public void addNotification(Integer userId, Notification notification) {
        User user = userRepository.findUserById(userId);

        if (user == null) throw new ApiException("User not found");

        notification.setUser(user);
        notification.setStatus("PENDING");

        notificationRepository.save(notification);
    }

    public void updateNotification(Integer id, Notification updateNotification) {
        Notification notification = notificationRepository.findNotificationById(id);

        if (notification == null) throw new ApiException("Notification not found");

        notification.setType(updateNotification.getType());
        notification.setChannel(updateNotification.getChannel());
        notification.setMessage(updateNotification.getMessage());
        notification.setStatus(updateNotification.getStatus());
        notification.setSentAt(updateNotification.getSentAt());

        notificationRepository.save(notification);
    }

    public void deleteNotification(Integer id) {
        Notification notification = notificationRepository.findNotificationById(id);

        if (notification == null) throw new ApiException("Notification not found");

        notificationRepository.delete(notification);
    }
}