package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.*;
import com.nawaf.capstone3.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final MaintenanceRuleRepository maintenanceRuleRepository;
    private final WhatsAppService whatsAppService;
    private final EmailService emailService;
    private final TransactionTemplate transactions;
    private final MaintenanceScheduleService schedules;
    private final jakarta.persistence.EntityManager entityManager;

    public List<Notification> getNotifications() { return notificationRepository.findAll(); }

    public Notification getNotificationById(Integer id) {
        var notification = notificationRepository.findNotificationById(id);
        if (notification == null) throw new ApiException("Notification not found");
        return notification;
    }

    public void addNotification(Integer userId, Notification input) {
        transactions.executeWithoutResult(status -> {
            var user = userRepository.findUserById(userId);
            if (user == null) throw new ApiException("User not found");
            rejectReservedMessage(input.getMessage());
            var notification = new Notification();
            notification.setType(input.getType());
            notification.setChannel(input.getChannel());
            notification.setMessage(input.getMessage());
            notification.setUser(user);
            notificationRepository.save(notification);
        });
    }

    public void updateNotification(Integer id, Notification input) {
        transactions.executeWithoutResult(status -> {
            var notification = lockedNotification(id);
            if (!"PENDING".equals(notification.getStatus()) || isGenerated(notification))
                throw new ApiException("Delivered or generated notifications cannot be edited");
            rejectReservedMessage(input.getMessage());
            notification.setType(input.getType());
            notification.setChannel(input.getChannel());
            notification.setMessage(input.getMessage());
            notificationRepository.save(notification);
        });
    }

    public void deleteNotification(Integer id) {
        transactions.executeWithoutResult(status -> {
            var notification = lockedNotification(id);
            if (isGenerated(notification)) throw new ApiException("Keep generated notification delivery history");
            notificationRepository.delete(notification);
        });
    }

    public List<Notification> checkMaintenance(Integer vehicleId) {
        return transactions.execute(status -> {
            Vehicle vehicle = lockedVehicle(vehicleId);
            var records = maintenanceRecordRepository.findMaintenanceRecordByVehicle(vehicle);
            List<Notification> result = new ArrayList<>();
            for (var rule : maintenanceRuleRepository.findMaintenanceRuleByVehicle(vehicle)) {
                var state = schedules.evaluate(vehicle, rule, records, LocalDate.now());
                if (!schedules.requiresReminder(state)) continue;
                String key = reminderKey(vehicleId, rule.getId(), state.cycle());
                Notification notification = notificationRepository.findTopByUserIdAndMessageStartingWithOrderByIdDesc(
                        vehicle.getUser().getId(), key);
                if (notification == null) {
                    notification = new Notification();
                    notification.setUser(vehicle.getUser());
                    notification.setChannel("WHATSAPP");
                    notification.setMaintenanceRecord(state.latest());
                } else if (!state.type().equals(notification.getType())) {
                    notification.setStatus("PENDING");
                }
                notification.setType(state.type());
                notification.setMessage(key + " " + state.type().replace('_', ' ') + ": " + rule.getServiceName()
                        + (rule.getKilometers() == null ? " (time schedule)" : " at " + rule.getKilometers() + " KM"));
                result.add(notificationRepository.save(notification));
            }
            return result;
        });
    }

    public List<Notification> checkAndSendMaintenance(Integer vehicleId) {
        return checkMaintenance(vehicleId).stream().map(n -> deliver(n.getId(), false)).toList();
    }

    public void checkAllVehicles() {
        for (Vehicle vehicle : vehicleRepository.findAll()) {
            try { checkAndSendMaintenance(vehicle.getId()); }
            catch (RuntimeException exception) {
                org.slf4j.LoggerFactory.getLogger(NotificationService.class)
                        .warn("Maintenance check failed for vehicle {}: {}", vehicle.getId(), exception.getClass().getSimpleName());
            }
        }
    }

    public String testMaintenanceWhatsApp(Integer vehicleId) {
        var notifications = checkAndSendMaintenance(vehicleId);
        return notifications.isEmpty() ? "No maintenance due" : "Checked " + notifications.size() + " tracked reminders";
    }

    public Notification retryNotification(Integer id) {
        Notification result = deliver(id, true);
        if ("FAILED".equals(result.getStatus())) throw new ApiException("Notification delivery failed");
        return result;
    }

    private Notification deliver(Integer id, boolean retry) {
        return transactions.execute(status -> {
            // Consistent lock order with maintenance writes, then notification lock across bounded delivery.
            var initial = getNotificationById(id);
            Integer vehicleId = generatedVehicleId(initial.getMessage());
            Vehicle vehicle = vehicleId == null ? null : lockedVehicle(vehicleId);
            var notification = lockedNotification(id);
            if (retry && !"FAILED".equals(notification.getStatus()))
                throw new ApiException("Notification is not failed");
            if (notification.getMessage().startsWith("[maintenance:")) {
                var records = maintenanceRecordRepository.findMaintenanceRecordByVehicle(vehicle);
                boolean active = maintenanceRuleRepository.findMaintenanceRuleByVehicle(vehicle).stream().anyMatch(rule -> {
                    var state = schedules.evaluate(vehicle, rule, records, LocalDate.now());
                    return schedules.requiresReminder(state)
                            && notification.getMessage().startsWith(reminderKey(vehicle.getId(), rule.getId(), state.cycle()));
                });
                if (!active) throw new ApiException("Reminder is no longer applicable");
            }
            if ("REPORT".equals(notification.getType()) && "SENT".equals(notification.getStatus())) return notification;
            if (!retry && notification.getSentAt() != null
                    && notification.getSentAt().plusDays(7).isAfter(LocalDateTime.now())
                    && "SENT".equals(notification.getStatus())) return notification;
            try {
                if ("WHATSAPP".equals(notification.getChannel())) {
                    whatsAppService.sendMessage(notification.getUser().getPhoneNumber(), notification.getMessage());
                } else if ("EMAIL".equals(notification.getChannel())) {
                    if ("REPORT".equals(notification.getType()) && vehicle != null)
                        emailService.sendMonthlyReport(notification.getUser().getEmail(), notification.getUser().getName(),
                                vehicle.getMake() + " " + vehicle.getModel(), notification.getMessage());
                    else emailService.sendText(notification.getUser().getEmail(), "Sayyan notification", notification.getMessage());
                } else throw new ApiException("Unsupported notification channel");
                notification.setStatus("SENT");
                notification.setSentAt(LocalDateTime.now());
            } catch (RuntimeException exception) {
                notification.setStatus("FAILED");
                notification.setSentAt(null);
            }
            return notificationRepository.saveAndFlush(notification);
        });
    }

    public String sendMonthlyReport(Integer vehicleId) {
        Integer id = transactions.execute(status -> {
            Vehicle vehicle = lockedVehicle(vehicleId);
            LocalDate end = LocalDate.now().withDayOfMonth(1);
            LocalDate start = end.minusMonths(1);
            if (vehicle.getCreatedAt() == null || vehicle.getCreatedAt().toLocalDate().isAfter(start)) return null;
            String key = "[report:" + vehicleId + ":" + start + "]";
            var notification = notificationRepository.findTopByUserIdAndMessageStartingWithOrderByIdDesc(vehicle.getUser().getId(), key);
            if (notification != null) return notification.getId();
            notification = new Notification();
            notification.setUser(vehicle.getUser());
            notification.setType("REPORT");
            notification.setChannel("EMAIL");
            notification.setMessage(key + "\n" + buildMonthlyReport(vehicle, start.atStartOfDay(), end.atStartOfDay()));
            return notificationRepository.saveAndFlush(notification).getId();
        });
        if (id == null) return "SKIPPED: no complete calendar month since registration";
        if ("SENT".equals(getNotificationById(id).getStatus())) return "SKIPPED: report already sent";
        return "SENT".equals(deliverReport(id).getStatus()) ? "SENT" : "FAILED";
    }

    private Notification deliverReport(Integer id) {
        // deliver() is serialized by the notification lock; REPORT is permanently idempotent after success.
        return deliver(id, false);
    }

    private String buildMonthlyReport(Vehicle vehicle, LocalDateTime startDate, LocalDateTime endDate
    ) {
        //كم صيانة تمت؟
        //كم مجموع تكلفتها؟
        int maintenanceCount = 0;
        double maintenanceCost = 0;

        //كم Receipt؟
        //كم مجموع مبالغها؟
        int receiptCount = 0;
        double receiptTotal = 0;

        //- كم Notification؟
        //- كم واحدة SENT؟
        //- كم واحدة FAILED؟
        int notificationCount = 0;
        int sentNotifications = 0;
        int failedNotifications = 0;

        //عشان يعرف بداية ونهاية الكيلومترات.
        int startKilometers = 0;
        int endKilometers = 0;

        for (MaintenanceRecord record : vehicle.getMaintenanceRecords()) {

            LocalDateTime serviceDate = record.getServiceDate().atStartOfDay();

            if (!serviceDate.isBefore(startDate) && serviceDate.isBefore(endDate)) {
                // إذا كانت الصيانة داخل الشهر
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

                if (startKilometers == 0 || record.getRecordedAt().isBefore(startDate)) {
                    startKilometers = record.getKilometers();
                }

                endKilometers = record.getKilometers();
            }
        }
        int distance = endKilometers - startKilometers;

        return """
        <div style="
            font-family: Arial, Helvetica, sans-serif;
            color: #24323D;
            background-color: #ffffff;
            width: 100%%;
        ">

            <!-- Report Header -->
            <div style="
                padding: 24px 0 22px 0;
                border-bottom: 1px solid #E6ECEB;
                margin-bottom: 20px;
            ">

                <div style="
                    font-size: 11px;
                    font-weight: bold;
                    letter-spacing: 1.5px;
                    color: #6B7D86;
                    text-transform: uppercase;
                    margin-bottom: 8px;
                ">
                    MONTHLY VEHICLE REPORT
                </div>

                <div style="
                    font-size: 24px;
                    font-weight: bold;
                    color: #123C3A;
                    margin-bottom: 6px;
                ">
                    %s %s
                </div>

                <div style="
                    font-size: 13px;
                    color: #718096;
                ">
                    Reporting period: %s — %s
                </div>

            </div>


            <!-- Overview -->
            <div style="
                font-size: 12px;
                font-weight: bold;
                color: #123C3A;
                letter-spacing: 1px;
                margin-bottom: 12px;
            ">
                OVERVIEW
            </div>

            <table width="100%%" cellpadding="0" cellspacing="0"
                   style="margin-bottom: 26px;">

                <tr>

                    <!-- Maintenance -->
                    <td width="33%%" valign="top"
                        style="padding-right: 7px;">

                        <div style="
                            background-color: #F4F8F7;
                            border: 1px solid #E2EBE9;
                            border-radius: 12px;
                            padding: 18px 14px;
                        ">

                            <div style="
                                color: #6B7D86;
                                font-size: 11px;
                                margin-bottom: 8px;
                            ">
                                SERVICES
                            </div>

                            <div style="
                                color: #123C3A;
                                font-size: 24px;
                                font-weight: bold;
                            ">
                                %d
                            </div>

                            <div style="
                                color: #8A989E;
                                font-size: 10px;
                                margin-top: 4px;
                            ">
                                Maintenance operations
                            </div>

                        </div>

                    </td>


                    <!-- Maintenance Cost -->
                    <td width="33%%" valign="top"
                        style="padding: 0 4px;">

                        <div style="
                            background-color: #F4F8F7;
                            border: 1px solid #E2EBE9;
                            border-radius: 12px;
                            padding: 18px 14px;
                        ">

                            <div style="
                                color: #6B7D86;
                                font-size: 11px;
                                margin-bottom: 8px;
                            ">
                                MAINTENANCE COST
                            </div>

                            <div style="
                                color: #123C3A;
                                font-size: 20px;
                                font-weight: bold;
                            ">
                                %.2f
                            </div>

                            <div style="
                                color: #8A989E;
                                font-size: 10px;
                                margin-top: 4px;
                            ">
                                SAR
                            </div>

                        </div>

                    </td>


                    <!-- Receipts -->
                    <td width="33%%" valign="top"
                        style="padding-left: 7px;">

                        <div style="
                            background-color: #F4F8F7;
                            border: 1px solid #E2EBE9;
                            border-radius: 12px;
                            padding: 18px 14px;
                        ">

                            <div style="
                                color: #6B7D86;
                                font-size: 11px;
                                margin-bottom: 8px;
                            ">
                                RECEIPTS
                            </div>

                            <div style="
                                color: #123C3A;
                                font-size: 24px;
                                font-weight: bold;
                            ">
                                %d
                            </div>

                            <div style="
                                color: #8A989E;
                                font-size: 10px;
                                margin-top: 4px;
                            ">
                                Recorded receipts
                            </div>

                        </div>

                    </td>

                </tr>

            </table>


            <!-- Maintenance Section -->
            <div style="
                border-bottom: 1px solid #E6ECEB;
                padding-bottom: 18px;
                margin-bottom: 22px;
            ">

                <div style="
                    color: #123C3A;
                    font-size: 15px;
                    font-weight: bold;
                    margin-bottom: 14px;
                ">
                    Maintenance
                </div>

                <table width="100%%" cellpadding="0" cellspacing="0">

                    <tr>
                        <td style="
                            color: #718096;
                            font-size: 13px;
                            padding: 8px 0;
                        ">
                            Services completed
                        </td>

                        <td align="right" style="
                            color: #24323D;
                            font-size: 13px;
                            font-weight: bold;
                            padding: 8px 0;
                        ">
                            %d
                        </td>
                    </tr>

                    <tr>
                        <td style="
                            color: #718096;
                            font-size: 13px;
                            padding: 8px 0;
                        ">
                            Total maintenance cost
                        </td>

                        <td align="right" style="
                            color: #123C3A;
                            font-size: 14px;
                            font-weight: bold;
                            padding: 8px 0;
                        ">
                            %.2f SAR
                        </td>
                    </tr>

                </table>

            </div>


            <!-- Receipts Section -->
            <div style="
                border-bottom: 1px solid #E6ECEB;
                padding-bottom: 18px;
                margin-bottom: 22px;
            ">

                <div style="
                    color: #123C3A;
                    font-size: 15px;
                    font-weight: bold;
                    margin-bottom: 14px;
                ">
                    Receipts
                </div>

                <table width="100%%" cellpadding="0" cellspacing="0">

                    <tr>
                        <td style="
                            color: #718096;
                            font-size: 13px;
                            padding: 8px 0;
                        ">
                            Number of receipts
                        </td>

                        <td align="right" style="
                            color: #24323D;
                            font-size: 13px;
                            font-weight: bold;
                            padding: 8px 0;
                        ">
                            %d
                        </td>
                    </tr>

                    <tr>
                        <td style="
                            color: #718096;
                            font-size: 13px;
                            padding: 8px 0;
                        ">
                            Total receipt amount
                        </td>

                        <td align="right" style="
                            color: #123C3A;
                            font-size: 14px;
                            font-weight: bold;
                            padding: 8px 0;
                        ">
                            %.2f SAR
                        </td>
                    </tr>

                </table>

            </div>


            <!-- Mileage Section -->
            <div style="
                border-bottom: 1px solid #E6ECEB;
                padding-bottom: 18px;
                margin-bottom: 22px;
            ">

                <div style="
                    color: #123C3A;
                    font-size: 15px;
                    font-weight: bold;
                    margin-bottom: 14px;
                ">
                    Mileage
                </div>

                <table width="100%%" cellpadding="0" cellspacing="0">

                    <tr>
                        <td style="
                            color: #718096;
                            font-size: 13px;
                            padding: 8px 0;
                        ">
                            Starting mileage
                        </td>

                        <td align="right" style="
                            color: #24323D;
                            font-size: 13px;
                            font-weight: bold;
                            padding: 8px 0;
                        ">
                            %,d KM
                        </td>
                    </tr>

                    <tr>
                        <td style="
                            color: #718096;
                            font-size: 13px;
                            padding: 8px 0;
                        ">
                            Ending mileage
                        </td>

                        <td align="right" style="
                            color: #24323D;
                            font-size: 13px;
                            font-weight: bold;
                            padding: 8px 0;
                        ">
                            %,d KM
                        </td>
                    </tr>

                    <tr>
                        <td style="
                            color: #718096;
                            font-size: 13px;
                            padding: 8px 0;
                        ">
                            Distance driven
                        </td>

                        <td align="right" style="
                            color: #123C3A;
                            font-size: 14px;
                            font-weight: bold;
                            padding: 8px 0;
                        ">
                            %,d KM
                        </td>
                    </tr>

                </table>

            </div>


            <!-- Notifications -->
            <div style="
                background-color: #F8FAF9;
                border-radius: 12px;
                padding: 18px 20px;
                margin-bottom: 8px;
            ">

                <div style="
                    color: #123C3A;
                    font-size: 15px;
                    font-weight: bold;
                    margin-bottom: 14px;
                ">
                    Notifications
                </div>

                <table width="100%%" cellpadding="0" cellspacing="0">

                    <tr>
                        <td style="
                            color: #718096;
                            font-size: 12px;
                            padding: 5px 0;
                        ">
                            Total notifications
                        </td>

                        <td align="right" style="
                            color: #24323D;
                            font-size: 12px;
                            font-weight: bold;
                        ">
                            %d
                        </td>
                    </tr>

                    <tr>
                        <td style="
                            color: #718096;
                            font-size: 12px;
                            padding: 5px 0;
                        ">
                            Successfully sent
                        </td>

                        <td align="right" style="
                            color: #2F6F68;
                            font-size: 12px;
                            font-weight: bold;
                        ">
                            %d
                        </td>
                    </tr>

                    <tr>
                        <td style="
                            color: #718096;
                            font-size: 12px;
                            padding: 5px 0;
                        ">
                            Failed
                        </td>

                        <td align="right" style="
                            color: #9B5C5C;
                            font-size: 12px;
                            font-weight: bold;
                        ">
                            %d
                        </td>
                    </tr>

                </table>

            </div>


            <!-- Closing -->
            <div style="
                text-align: center;
                padding: 26px 0 8px 0;
            ">

                <div style="
                    color: #123C3A;
                    font-size: 13px;
                    font-weight: bold;
                ">
                    Sayan
                </div>

                <div style="
                    color: #8A989E;
                    font-size: 11px;
                    margin-top: 5px;
                ">
                    Smart vehicle care, made simple.
                </div>

            </div>

        </div>
        """.formatted(
                vehicle.getMake(),
                vehicle.getModel(),
                startDate.toLocalDate(),
                endDate.toLocalDate(),

                maintenanceCount,
                maintenanceCost,
                receiptCount,

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

    private String sum(List<Double> amounts) {
        return amounts.stream().map(java.math.BigDecimal::valueOf).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private boolean inPeriod(LocalDate date, LocalDate start, LocalDate end) { return !date.isBefore(start) && date.isBefore(end); }
    private String reminderKey(Integer vehicleId, Integer ruleId, String cycle) {
        return "[maintenance:" + vehicleId + ":" + ruleId + ":" + cycle + "]";
    }
    private boolean isGenerated(Notification n) { return generatedVehicleId(n.getMessage()) != null; }
    private void rejectReservedMessage(String message) {
        if (message != null && (message.startsWith("[maintenance:") || message.startsWith("[report:")))
            throw new ApiException("Reserved notification message prefix");
    }
    private Integer generatedVehicleId(String message) {
        if (message == null || !(message.startsWith("[maintenance:") || message.startsWith("[report:"))) return null;
        try { return Integer.valueOf(message.split(":", 3)[1]); }
        catch (RuntimeException exception) { return null; }
    }
    private Notification lockedNotification(Integer id) {
        var n = notificationRepository.findNotificationForUpdate(id);
        if (n == null) throw new ApiException("Notification not found");
        entityManager.refresh(n);
        return n;
    }
    private Vehicle lockedVehicle(Integer id) {
        entityManager.flush();
        var vehicle = vehicleRepository.findVehicleForUpdate(id);
        if (vehicle == null) throw new ApiException("Vehicle not found");
        entityManager.refresh(vehicle);
        return vehicle;
    }

    public void testMonthlyReport(Integer vehicleId) {

        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) {
            throw new ApiException("Vehicle not found");
        }

        LocalDateTime startDate = vehicle.getCreatedAt();
        LocalDateTime endDate = LocalDateTime.now();

        String report = buildMonthlyReport(vehicle, startDate, endDate);

        emailService.sendMonthlyReport(
                vehicle.getUser().getEmail(),
                vehicle.getUser().getName(),
                vehicle.getMake() + " " + vehicle.getModel(),
                report
        );
    }
}
