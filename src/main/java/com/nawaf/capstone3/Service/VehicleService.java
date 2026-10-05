package com.nawaf.capstone3.Service;


import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.User;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.UserRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;



    public List<Vehicle>getAllVehicle(){
        return vehicleRepository.findAll();
    }



    // Get all vehicles for a user
    public List<Vehicle>getAllVehicleForUser(Integer userId){
        User user=userRepository.findUsersById(userId);
        if(user==null){
            throw new ApiException("user not found");
        }
        return vehicleRepository.findVehicleByUser(user);
    }



    public void addVehicle(Integer userId,Vehicle vehicle){
        User user=userRepository.findUsersById(userId);
        if(user==null){
            throw new ApiException("User not found");
        }
        vehicle.setUser(user);
        vehicleRepository.save(vehicle);
    }


    public void updateVehicle(Integer userId,Integer vehicleId, Vehicle updateVehicle){

        User user=userRepository.findUsersById(userId);
        if(user ==null){
            throw new ApiException("user not found");
        }

        Vehicle vehicle=vehicleRepository.findVehicleById(vehicleId);
        if(vehicle==null){
            throw new ApiException("vehicle not found");
        }

        vehicle.setVin(updateVehicle.getVin());
        vehicle.setMake(updateVehicle.getMake());
        vehicle.setModel(updateVehicle.getModel());
        vehicle.setYear(updateVehicle.getYear());
        vehicle.setEngine(updateVehicle.getEngine());
        vehicle.setFuelType(updateVehicle.getFuelType());
        vehicle.setCurrentKilometers(updateVehicle.getCurrentKilometers());
        vehicleRepository.save(vehicle);
    }


    public void delete(Integer userId,Integer vehicleId){
        User user=userRepository.findUsersById(userId);
        if(user==null){
            throw new ApiException("user not found");
        }

        // Check that the vehicle belongs to the user before deleting it
        Vehicle vehicle=vehicleRepository.findVehicleByIdAndUser(vehicleId,user);
          if(vehicle==null){
            throw new ApiException("Vehicle not found or does not belong to this user");
          }
        vehicleRepository.delete(vehicle);
    }
}
