package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRuleRepository extends JpaRepository<MaintenanceRule, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from MaintenanceRule r where r.id = :id")
    MaintenanceRule findRuleForUpdate(@Param("id") Integer id);

    MaintenanceRule findMaintenanceRuleById(Integer id);
    List<MaintenanceRule>findMaintenanceRuleByVehicle(Vehicle vehicle);
}
