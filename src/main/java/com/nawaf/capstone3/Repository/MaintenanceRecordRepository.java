package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord,Integer> {
    MaintenanceRecord findMaintenanceRecordById(Integer id);
}