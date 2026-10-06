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

    public List<Notification> getAllNotifications() {
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

        notificationRepository.save(notification);
    }

    public void updateNotification(Integer id, Notification notification) {
        Notification oldNotification = getNotificationById(id);

        oldNotification.setType(notification.getType());
        oldNotification.setChannel(notification.getChannel());
        oldNotification.setMessage(notification.getMessage());
        oldNotification.setStatus(notification.getStatus());
        oldNotification.setSentAt(notification.getSentAt());

        notificationRepository.save(oldNotification);
    }

    public void deleteNotification(Integer id) {
        Notification notification = getNotificationById(id);

        notificationRepository.delete(notification);
    }
}