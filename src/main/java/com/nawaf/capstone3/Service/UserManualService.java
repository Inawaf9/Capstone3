package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.MaintenanceRule;
import com.nawaf.capstone3.Model.UserManual;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.MaintenanceRuleRepository;
import com.nawaf.capstone3.Repository.UserManualRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserManualService {
    private final UserManualRepository userManualRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRuleRepository maintenanceRuleRepository;


    public List<UserManual> getAll(){
        return userManualRepository.findAll();
    }
    //تحققنا من ان اي دي الخاص بالمركبة موجود وبعدها تم الربط
    public void addUserManual(Integer vehicleId, UserManual userManual) {

        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) {
            throw new ApiException("vehicle id not found");
        }

        userManual.setVehicle(vehicle);

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

        userManualRepository.save(oldUserManual);
    }

    public void deleteUserManual(Integer userManualId){
        UserManual oldUserManual=userManualRepository.findUserManualById(userManualId);
        if(oldUserManual==null){
            throw new ApiException("user id not found , can not be deleted");
        }
        userManualRepository.deleteById(userManualId);
    }

    //4
    public List<MaintenanceRule> getRulesByUserManualId(Integer userManualId) {
        UserManual userManual = userManualRepository.findUserManualById(userManualId);

        if (userManual == null) {
            throw new ApiException("user manual id not found");
        }
        //اذا الحالة مازالت في التحليل يفرق بيها المستخدم من الحالة الفارغة
        if (!"COMPLETED".equals(userManual.getStatus())) {
            throw new ApiException("manual analysis is not completed yet, current status: " + userManual.getStatus());
        }

        return maintenanceRuleRepository.findMaintenanceRulesByUserManualIdOrderByServiceName(userManualId);
    }


}