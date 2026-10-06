package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.*;
import com.nawaf.capstone3.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final WhatsAppService whatsAppService;
    private final EmailService emailService;
    private final UserManualRepository userManualRepository;

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Notification getNotificationById(Integer id) {
        Notification notification = notificationRepository.findNotificationById(id);

        if (notification == null) throw new ApiException("Notification not found");

        return notification;
    }

    public void addNotification(Integer userId, Notification notification) {
        User user = userRepository.findUsersById(userId);

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




    public List<Notification> checkMaintenance(Integer vehicleId) {

        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) {
            throw new ApiException("Vehicle not found");
        }

        List<MaintenanceRecord> records =
                maintenanceRecordRepository.findMaintenanceRecordByVehicle(vehicle);

        List<Notification> notifications = new ArrayList<>();

        for (MaintenanceRecord record : records) {

            MaintenanceRule rule = record.getMaintenanceRule();

            boolean due = false;

            // فحص الصيانة حسب الكيلومترات
            if (rule.getTriggerType().equals("KILOMETER")) {

                due = vehicle.getCurrentKilometers() - record.getKilometers()
                        >= rule.getKilometerInterval();
            }

            // فحص الصيانة حسب الوقت
            if (rule.getTriggerType().equals("TIME")) {

                due = !LocalDate.now().isBefore(
                        record.getServiceDate()
                                .plusMonths(rule.getMonthInterval())
                );
            }

            if (due) {

                Notification pendingNotification = notificationRepository.findTopByMaintenanceRecordIdAndStatus(record.getId(), "PENDING");
                if (pendingNotification != null) {
                    // فيه تنبيه PENDING موجود، نرجعه
                    notifications.add(pendingNotification);

                } else {
                    // ما فيه PENDING، ننشئ واحد جديد
                    Notification notification = new Notification();

                    notification.setType("MAINTENANCE_DUE");
                    notification.setChannel("WHATSAPP");
                    notification.setStatus("PENDING");

                    notification.setMessage("Maintenance Due: " + rule.getServiceName());

                    notification.setUser(vehicle.getUser());
                    notification.setMaintenanceRecord(record);

                    notifications.add(notificationRepository.save(notification)
                    );
                }
            }
            }


        return notifications;
    }

    public List<Notification> checkAndSendMaintenance(Integer vehicleId) {

        List<Notification> notifications = checkMaintenance(vehicleId);

        for (Notification notification : notifications) {

            if (notification.getStatus().equals("PENDING")) {
                Notification lastNotification = notificationRepository.findTopByMaintenanceRecordIdAndStatusOrderBySentAtDesc(notification.getMaintenanceRecord().getId(), "SENT");


                boolean canSend = lastNotification == null || lastNotification.getSentAt() == null || !lastNotification.getSentAt().plusDays(7).isAfter(LocalDateTime.now());
                if (canSend) {
                    try {
                        String phoneNumber = notification.getUser().getPhoneNumber();

                        whatsAppService.sendMessage(phoneNumber, notification.getMessage()
                        );

                        notification.setStatus("SENT");
                        notification.setSentAt(LocalDateTime.now());

                        notificationRepository.save(notification);

                    } catch (Exception e) {

                        notification.setStatus("FAILED");
                        notificationRepository.save(notification);
                    }
                }
            }
        }
        return notifications;
    }


    public void checkAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        for (Vehicle vehicle : vehicles) {
            checkAndSendMaintenance(vehicle.getId());
        }
    }



    public Notification retryNotification(Integer notificationId) {

        Notification notification = getNotificationById(notificationId);

        // نسمح بإعادة الإرسال فقط إذا كان الإرسال السابق فاشل
        if (!notification.getStatus().equals("FAILED")) {
            throw new ApiException("Notification is not failed");
        }

        try {
            String phoneNumber = notification.getUser().getPhoneNumber();

            whatsAppService.sendMessage(
                    phoneNumber,
                    notification.getMessage()
            );

            // إذا نجح الإرسال
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());

            return notificationRepository.save(notification);

        } catch (Exception e) {

            // إذا فشلت المحاولة مرة ثانية
            notification.setStatus("FAILED");
            notificationRepository.save(notification);
            throw new ApiException("Failed to resend notification");
        }
    }


    public void sendMonthlyReport(Integer vehicleId) {

        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) {
            throw new ApiException("Vehicle not found");
        }

        UserManual firstManual =
                userManualRepository.findTopByVehicleIdOrderByUploadedAtAsc(vehicleId);

        if (firstManual == null) {
            throw new ApiException("Vehicle has no user manual");
        }

        // تاريخ أول User Manual = بداية دورة التقارير
        LocalDateTime firstDate = firstManual.getUploadedAt();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startDate = firstDate;
        LocalDateTime endDate = startDate.plusMonths(1);

        // نحدد آخر شهر مكتمل
        while (!endDate.isAfter(now)) {
            startDate = endDate;
            endDate = startDate.plusMonths(1);
        }

        // نرجع للشهر المكتمل الأخير
        startDate = startDate.minusMonths(1);
        endDate = startDate.plusMonths(1);

        String report = buildMonthlyReport(
                vehicle,
                startDate,
                endDate
        );

        emailService.sendMonthlyReport(
                vehicle.getUser().getEmail(),
                vehicle.getUser().getName(),
                vehicle.getMake() + " " + vehicle.getModel(),
                report
        );

        // نسجل أن التقرير تم إرساله
        Notification notification = new Notification();

        notification.setType("REPORT");
        notification.setChannel("EMAIL");
        notification.setStatus("SENT");
        notification.setMessage("Monthly vehicle report");
        notification.setSentAt(LocalDateTime.now());
        notification.setUser(vehicle.getUser());

        notificationRepository.save(notification);
    }


    private String buildMonthlyReport(
            Vehicle vehicle,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {

        int maintenanceCount = 0;
        double maintenanceCost = 0;

        int receiptCount = 0;
        double receiptTotal = 0;

        int notificationCount = 0;
        int sentNotifications = 0;
        int failedNotifications = 0;

        int startKilometers = 0;
        int endKilometers = 0;

        for (MaintenanceRecord record : vehicle.getMaintenanceRecords()) {

            LocalDateTime serviceDate = record.getServiceDate().atStartOfDay();

            if (!serviceDate.isBefore(startDate) && serviceDate.isBefore(endDate)) {
                maintenanceCount++;
                maintenanceCost += record.getCost();

                if (record.getReceipts() != null) {

                    for (Receipt receipt : record.getReceipts()) {

                        LocalDate receiptDate = receipt.getExtractedDate();

                        if (!receiptDate.isBefore(startDate.toLocalDate()) && receiptDate.isBefore(endDate.toLocalDate())) {

                            receiptCount++;
                            receiptTotal += receipt.getTotalAmount();
                        }
                    }
                }

                if (record.getNotifications() != null) {

                    for (Notification notification : record.getNotifications()) {

                        if (notification.getCreatedAt() == null) {
                            continue;
                        }

                        if (!notification.getCreatedAt().isBefore(startDate) && notification.getCreatedAt().isBefore(endDate)) {

                            notificationCount++;

                            if ("SENT".equals(notification.getStatus())) {
                                sentNotifications++;
                            }

                            if ("FAILED".equals(notification.getStatus())) {
                                failedNotifications++;
                            }
                        }
                    }
                }
            }
        }

        for (KilometerRecord record : vehicle.getKilometerRecords()) {

            if (record.getRecordedAt() == null) {
                continue;
            }

            if (!record.getRecordedAt().isBefore(startDate)
                    && record.getRecordedAt().isBefore(endDate)) {

                if (startKilometers == 0) {
                    startKilometers = record.getKilometers();
                }

                endKilometers = record.getKilometers();
            }
        }
        int distance = endKilometers - startKilometers;

        return """
            Monthly Vehicle Report
            ----------------------

            Vehicle: %s %s
            Period: %s → %s

            Maintenance
            - Services: %d
            - Total Cost: %.2f SAR

            Receipts
            - Number: %d
            - Total: %.2f SAR

            Kilometers
            - Start: %d KM
            - End: %d KM
            - Distance: %d KM

            Notifications
            - Total: %d
            - Sent: %d
            - Failed: %d
            """.formatted(
                vehicle.getMake(),
                vehicle.getModel(),
                startDate.toLocalDate(),
                endDate.toLocalDate(),
                maintenanceCount,
                maintenanceCost,
                receiptCount,
                receiptTotal,
                startKilometers,
                endKilometers,
                distance,
                notificationCount,
                sentNotifications,
                failedNotifications
        );
      }
    }

