package com.nawaf.capstone3.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaintenanceScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 * * * *")
    public void automaticMaintenanceCheck() {

        notificationService.checkAllVehicles();
    }
}