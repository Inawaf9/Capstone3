package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.KilometerRecord;
<<<<<<< Updated upstream
=======
import com.nawaf.capstone3.Model.User;
>>>>>>> Stashed changes
import com.nawaf.capstone3.Model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KilometerRecordRepository extends JpaRepository<KilometerRecord, Integer> {

    KilometerRecord findKilometerRecordById(Integer id);

<<<<<<< Updated upstream
    KilometerRecord findKilometerRecordByIdAndVehicle(Integer id, Vehicle vehicle);

    List<KilometerRecord> findKilometerRecordsByVehicle(Vehicle vehicle);

    KilometerRecord findTopByVehicleOrderByRecordedAtDesc(Vehicle vehicle);

    List<KilometerRecord> findAllByVehicleOrderByRecordedAtDesc(Vehicle vehicle);

    List<KilometerRecord> findAllByVehicleAndRecordedAtBetweenOrderByRecordedAtAsc(Vehicle vehicle, LocalDateTime start, LocalDateTime end);
=======

>>>>>>> Stashed changes
}