package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.UserManual;
import com.nawaf.capstone3.Repository.UserManualRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserManualService {
    private final UserManualRepository userManualRepository;

      public List<UserManual> getAll(){
          return userManualRepository.findAll();
      }

      public void addUserManual(UserManual userManual){
          userManualRepository.save(userManual);
      }

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
}