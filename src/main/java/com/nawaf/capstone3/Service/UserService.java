package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.User;
import com.nawaf.capstone3.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;




    public List<User>getAllUser(){
        return userRepository.findAll();
    }


    public void addUser(User user){

        if (userRepository.findUsersByEmail(user.getEmail())!= null) {
            throw new ApiException("Email already exists");
        }
        if (userRepository.findUsersByPhoneNumber(user.getPhoneNumber()) != null) {
            throw new ApiException("Phone number already exists");
        }
        userRepository.save(user);
    }


    public void  update(Integer userId,User updateUser){
        User user=userRepository.findUsersById(userId);

        if(user ==null){
            throw new ApiException("User not found ");
        }

        User emailUser=userRepository.findUsersByEmail(updateUser.getEmail());
         if(emailUser!=null&&!emailUser.getId().equals(userId)){
             throw new ApiException("Email already exists");
        }

        User phoneUser=userRepository.findUsersByPhoneNumber(updateUser.getPhoneNumber());
        if(phoneUser!=null&&!phoneUser.getId().equals(userId)){
            throw new ApiException("Phone number already exists");
        }

        user.setName(updateUser.getName());
        user.setPassword(updateUser.getPassword());
        user.setEmail(updateUser.getEmail());
        user.setPhoneNumber(updateUser.getPhoneNumber());
        userRepository.save(user);
    }


    public void delete(Integer userId){
        User user=userRepository.findUsersById(userId);
        if(user ==null){
            throw new ApiException("User not found ");
        }
        userRepository.delete(user);
    }
}
