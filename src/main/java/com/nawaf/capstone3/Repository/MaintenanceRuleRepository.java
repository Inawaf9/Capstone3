package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.Vehicle;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRuleRepository extends JpaRepository<MaintenanceRule, Integer> {

    MaintenanceRule findMaintenanceRuleById(Integer id);
    List<MaintenanceRule>findMaintenanceRuleByVehicle(Vehicle vehicle);
}

    @Transactional
    void deleteByUserManualId(Integer userManualId);}
