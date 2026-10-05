package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.KilometerRecord;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.KilometerRecordRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class KilometerRecordService {

    private final KilometerRecordRepository kilometerRecordRepository;
    private final VehicleRepository vehicleRepository;


    public List<KilometerRecord> get() {
        return kilometerRecordRepository.findAll();
    }


    public void add(Integer vehicleId, KilometerRecord kilometerRecord) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) {
            throw new ApiException("Vehicle not found");
        }
        kilometerRecord.setVehicle(vehicle);
        kilometerRecordRepository.save(kilometerRecord);
    }


    public void update(Integer kilometerRecordId, KilometerRecord updateKilometerRecord) {

        KilometerRecord kilometerRecord = kilometerRecordRepository.findKilometerRecordById(kilometerRecordId);

        if (kilometerRecord == null) {
            throw new ApiException("Kilometer record not found");
        }

        kilometerRecord.setKilometers(updateKilometerRecord.getKilometers());
        kilometerRecord.setNote(updateKilometerRecord.getNote());

        kilometerRecordRepository.save(kilometerRecord);
    }


    public void delete(Integer kilometerRecordId) {

        KilometerRecord kilometerRecord = kilometerRecordRepository.findKilometerRecordById(kilometerRecordId);

        if (kilometerRecord == null) {
            throw new ApiException("Kilometer record not found");
        }
        kilometerRecordRepository.delete(kilometerRecord);
    }
}