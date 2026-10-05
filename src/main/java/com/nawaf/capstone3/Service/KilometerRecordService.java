package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.KilometerRecord;
import com.nawaf.capstone3.Model.User;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.KilometerRecordRepository;
import com.nawaf.capstone3.Repository.UserRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class KilometerRecordService {

    private final KilometerRecordRepository kilometerRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;


    public List<KilometerRecord> get() {
        return kilometerRecordRepository.findAll();
    }


    public void add(Integer userId,Integer vehicleId, KilometerRecord kilometerRecord) {
        User user=userRepository.findUsersById(userId);
        if(user==null){
            throw new ApiException(" user not found ");
        }
        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId,user);
        if (vehicle == null) {
            throw new ApiException("Vehicle not found or does not belong to this user");
        }
        kilometerRecord.setVehicle(vehicle);
        kilometerRecordRepository.save(kilometerRecord);
    }


    public void update(Integer userId ,Integer vehicleId,Integer kilometerRecordId, KilometerRecord updateKilometerRecord) {

        User user=userRepository.findUsersById(userId);
        if(user==null){
            throw new ApiException(" user not found ");
        }

        Vehicle vehicle=vehicleRepository.findVehicleByIdAndUser(vehicleId,user);
        if (vehicle == null) {
            throw new ApiException("Vehicle not found or does not belong to this user");
        }

        KilometerRecord kilometerRecord = kilometerRecordRepository.findKilometerRecordByIdAndVehicle(kilometerRecordId,vehicle);
        if (kilometerRecord == null) {
            throw new ApiException("Kilometer record not found or does not belong to this vehicle");
        }

        kilometerRecord.setKilometers(updateKilometerRecord.getKilometers());
        kilometerRecord.setNote(updateKilometerRecord.getNote());

        kilometerRecordRepository.save(kilometerRecord);
    }


    public void delete(Integer userId, Integer vehicleId, Integer kilometerRecordId) {

        User user = userRepository.findUsersById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        Vehicle vehicle = vehicleRepository.findVehicleByIdAndUser(vehicleId, user);
        if (vehicle == null) {
            throw new ApiException("Vehicle not found or does not belong to this user");
        }

        KilometerRecord kilometerRecord = kilometerRecordRepository.findKilometerRecordByIdAndVehicle(kilometerRecordId, vehicle);
        if (kilometerRecord == null) {
            throw new ApiException("Kilometer record not found or does not belong to this vehicle");
        }
        kilometerRecordRepository.delete(kilometerRecord);
    }
}