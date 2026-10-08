package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.MaintenanceRule;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRuleRepository extends JpaRepository<MaintenanceRule, Integer> {

    MaintenanceRule findMaintenanceRuleById(Integer id);

    @Transactional
    void deleteByUserManualId(Integer userManualId);}