package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.MaintenanceRecord;
import com.nawaf.capstone3.Model.Notification;
import com.nawaf.capstone3.Model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Integer> {

    MaintenanceRecord findMaintenanceRecordById(Integer id);
    List<MaintenanceRecord> findMaintenanceRecordByVehicle(Vehicle vehicle);

    @Query("""
    SELECT COALESCE(SUM(m.cost), 0)
    FROM MaintenanceRecord m
    WHERE m.vehicle.id = :vehicleId
    AND YEAR(m.serviceDate) = :year
    """)
    Double getTotalCostByVehicleAndYear(@Param("vehicleId") Integer vehicleId, @Param("year") Integer year);
    MaintenanceRecord findTopByVehicleIdOrderByServiceDateDesc(Integer vehicleId);


    boolean existsByMaintenanceRule_UserManual_Id(Integer manualId);
}