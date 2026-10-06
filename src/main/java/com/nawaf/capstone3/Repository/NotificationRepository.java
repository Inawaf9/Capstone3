package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    Notification findNotificationById(Integer id);
    Notification findTopByMaintenanceRecordIdAndStatusOrderBySentAtDesc(Integer maintenanceRecordId, String status);
    Notification findTopByMaintenanceRecordIdAndStatus(Integer maintenanceRecordId, String status);
    Notification findTopByVehicleIdAndTypeOrderBySentAtDesc(Integer VehicleId ,String status);

}
