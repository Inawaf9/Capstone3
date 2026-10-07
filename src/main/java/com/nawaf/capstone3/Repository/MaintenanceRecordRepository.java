package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.MaintenanceRecord;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Integer> {

    MaintenanceRecord findMaintenanceRecordById(Integer id);

    boolean existsByMaintenanceRule_UserManual_Id(Integer manualId);

    // الوصول لـ serviceName عن طريق MaintenanceRule
    // البحث بأحدث الكيلومترات

    MaintenanceRecord findFirstByVehicleIdAndMaintenanceRule_ServiceNameOrderByKilometersDesc(Integer vehicleId, String serviceName);


    // إرجاع جميع سجلات الصيانة لمركبة معينة مرتبة حسب الأحدث باستعمال serviceDate
    List<MaintenanceRecord> findByVehicleIdOrderByServiceDateDesc(Integer vehicleId);

    // البحث بأحدث تاريخ صيانة باستعمال serviceDate
    //for next maintenance

    MaintenanceRecord findFirstByVehicleIdAndMaintenanceRule_ServiceNameOrderByServiceDateDesc(Integer vehicleId, String serviceName);
}