package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.Notification;
import com.nawaf.capstone3.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;


    public List<Notification> getAllNotifications(){
        List<Notification> notifications = notificationRepository.findAll();

        if(notifications.isEmpty()) throw new ApiException("Notifications not found");

        return notifications;
    }

    public Notification getNotification(Integer id){
        Notification notification = notificationRepository.findNotificationById(id);

        if(notification == null) throw new ApiException("Notification not found");

        return notification;
    }

    public void createNotification(Notification notification){
        notificationRepository.save(notification);
    }

    public void updateNotification(Integer id, Notification notification) {
        Notification oldNotification = getNotification(id);

        oldNotification.setType(notification.getType());
        oldNotification.setChannel(notification.getChannel());
        oldNotification.setMessage(notification.getMessage());
        oldNotification.setStatus(notification.getStatus());
        oldNotification.setSentAt(notification.getSentAt());

        notificationRepository.save(oldNotification);
    }

    public void deleteNotification(Integer id){
        Notification notification = getNotification(id);

        notificationRepository.delete(notification);
    }
}
