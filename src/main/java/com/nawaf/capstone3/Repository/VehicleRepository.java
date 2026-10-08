package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.User;
import com.nawaf.capstone3.Model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehicle v where v.id = :id")
    Vehicle findVehicleForUpdate(@Param("id") Integer id);

    Vehicle findVehicleById(Integer id);

    Vehicle findVehicleByIdAndUser(Integer id, User user);

    Vehicle findVehicleByVin(String vin);

    List<Vehicle> findVehiclesByUser(User user);
}
