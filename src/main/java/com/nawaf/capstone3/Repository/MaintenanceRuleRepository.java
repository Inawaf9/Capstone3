package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.MaintenanceRule;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRuleRepository extends JpaRepository<MaintenanceRule, Integer> {

    MaintenanceRule findMaintenanceRuleById(Integer id);

    @Transactional
    void deleteByUserManualId(Integer userManualId);
    // 4
    List<MaintenanceRule> findMaintenanceRulesByUserManualIdOrderByServiceName(Integer userManualId);

    // جديد: قواعد مركبة عبر كل كتيباتها 5
    List<MaintenanceRule> findByUserManual_Vehicle_Id(Integer vehicleId);
//7 جلب مستخدم عن طريق الاي دي اخاص بالمركبة
    List<MaintenanceRule> findByUserManualVehicleId(Integer vehicleId);
}