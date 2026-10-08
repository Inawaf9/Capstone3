package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.UserManual;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.UserManualRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserManualService {

    private final UserManualRepository userManualRepository;
    private final VehicleRepository vehicleRepository;

<<<<<<< Updated upstream
    public List<UserManual> getUserManuals() {
        return userManualRepository.findAll();
    }
=======
      public List<UserManual> getAll(){
          return userManualRepository.findAll();
      }
     //تحققنا من ان اي دي الخاص بالمركبة موجود وبعدها تم الربط
      public void addUserManual(UserManual userManual){
          Vehicle vehicle=vehicleRepository.findVehicleById(userManual.getVehicle().getId());
          if(vehicle==null){
              throw new ApiException("vehicle id not found ");
          }
          userManualRepository.save(userManual);
      }
      public UserManual getUserManualById(Integer userManualId){
          UserManual userManual=userManualRepository.findUserManualById(userManualId);
          if(userManual==null){
              throw new ApiException("user manual id not found");
          }
          return userManual;
      }

      //نتناقش ايش لازم نعدل واذا التعديلات الثانية لازمها اند بوينت مختلفة
      public void updateUserManual(Integer userManualId, UserManual userManual){
          UserManual oldUserManual=userManualRepository.findUserManualById(userManualId);
          if(oldUserManual==null){
              throw new ApiException("userManual id not found ");
          }
          oldUserManual.setFileUrl(userManual.getFileUrl());
          oldUserManual.setMaintenanceRules(userManual.getMaintenanceRules());
          oldUserManual.setStatus(userManual.getStatus());
          oldUserManual.setVehicle(userManual.getVehicle());
          userManual.setUploadedAt(userManual.getUploadedAt());
>>>>>>> Stashed changes

    public UserManual getUserManualById(Integer id) {
        UserManual userManual = userManualRepository.findUserManualById(id);

        if (userManual == null) throw new ApiException("User manual not found");

        return userManual;
    }

    public void addUserManual(Integer vehicleId, UserManual userManual) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) throw new ApiException("Vehicle not found");

        userManual.setVehicle(vehicle);
        userManual.setStatus("UPLOADED");

        userManualRepository.save(userManual);
    }

    public void updateUserManual(Integer id, UserManual updateUserManual) {
        UserManual userManual = userManualRepository.findUserManualById(id);

        if (userManual == null) throw new ApiException("User manual not found");

        userManual.setFileUrl(updateUserManual.getFileUrl());
        userManual.setStatus(updateUserManual.getStatus());

        userManualRepository.save(userManual);
    }

    public void deleteUserManual(Integer id) {
        UserManual userManual = userManualRepository.findUserManualById(id);

        if (userManual == null) throw new ApiException("User manual not found");

        userManualRepository.delete(userManual);
    }
}