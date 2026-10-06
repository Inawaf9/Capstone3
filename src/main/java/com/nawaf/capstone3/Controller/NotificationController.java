package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.Notification;
import com.nawaf.capstone3.Service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllNotifications() {
        return ResponseEntity.status(200).body(notificationService.getAllNotifications());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getNotificationById(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(notificationService.getNotificationById(id));
    }


    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addNotification(@PathVariable Integer userId, @Valid @RequestBody Notification notification) {
        notificationService.addNotification(userId, notification);

        return ResponseEntity.status(201).body(new ApiResponse("Notification added successfully"));
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateNotification(@PathVariable Integer id, @Valid @RequestBody Notification notification) {
        notificationService.updateNotification(id, notification);

        return ResponseEntity.status(200).body(new ApiResponse("Notification updated successfully"));
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);

        return ResponseEntity.status(200).body(new ApiResponse("Notification deleted successfully"));
    }


    @PostMapping("/check/{vehicleId}")
    public ResponseEntity<?>checkMaintenance(@PathVariable Integer vehicleId){
        return ResponseEntity.status(200).body(notificationService.checkMaintenance(vehicleId));
    }

    @PostMapping("/check-and-send/{vehicleId}")
    public ResponseEntity<?>checkAndSendMaintenance(@PathVariable Integer vehicleId){
        return ResponseEntity.status(200).body(notificationService.checkAndSendMaintenance(vehicleId));
    }

    @PostMapping("/retry/{notificationId}")
    private ResponseEntity<?>retryNotification(@PathVariable Integer notificationId){
        return ResponseEntity.status(200).body(notificationService.retryNotification(notificationId));
    }


    @PostMapping("/vehicle/{vehicleId}/monthly-report")
    public ResponseEntity<?> sendMonthlyReport(@PathVariable Integer vehicleId) {

        notificationService.sendMonthlyReport(vehicleId);

        return ResponseEntity.status(200).body(new ApiResponse("Monthly report sent successfully"));
    }
}