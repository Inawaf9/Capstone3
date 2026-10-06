package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.UserManual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserManualRepository extends JpaRepository<UserManual, Integer> {

    UserManual findUserManualById(Integer id);
    UserManual findTopByVehicleIdOrderByUploadedAtAsc(Integer vehicleId);
}