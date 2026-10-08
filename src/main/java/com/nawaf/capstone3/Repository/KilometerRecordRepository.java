package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.KilometerRecord;
import com.nawaf.capstone3.Model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KilometerRecordRepository extends JpaRepository<KilometerRecord, Integer> {

    KilometerRecord findKilometerRecordByIdAndVehicle(Integer id, Vehicle vehicle);

    List<KilometerRecord> findKilometerRecordsByVehicle(Vehicle vehicle);

    KilometerRecord findTopByVehicleOrderByRecordedAtDescIdDesc(Vehicle vehicle);

    List<KilometerRecord> findAllByVehicleOrderByRecordedAtDescIdDesc(Vehicle vehicle);

}
