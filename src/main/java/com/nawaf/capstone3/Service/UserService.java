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

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer id) {
        User user = userRepository.findUserById(id);

        if (user == null) throw new ApiException("User not found");

        return user;
    }

    public void addUser(User user) {
        User emailUser = userRepository.findUserByEmail(user.getEmail());
        User phoneUser = userRepository.findUserByPhoneNumber(user.getPhoneNumber());

        if (emailUser != null) throw new ApiException("Email already exists");
        if (phoneUser != null) throw new ApiException("Phone number already exists");

        userRepository.save(user);
    }

    public void updateUser(Integer id, User updateUser) {
        User user = userRepository.findUserById(id);

        if (user == null) throw new ApiException("User not found");

        User emailUser = userRepository.findUserByEmail(updateUser.getEmail());
        User phoneUser = userRepository.findUserByPhoneNumber(updateUser.getPhoneNumber());

        if (emailUser != null && !emailUser.getId().equals(id)) throw new ApiException("Email already exists");
        if (phoneUser != null && !phoneUser.getId().equals(id)) throw new ApiException("Phone number already exists");

        user.setName(updateUser.getName());
        user.setEmail(updateUser.getEmail());
        user.setPhoneNumber(updateUser.getPhoneNumber());
        user.setPassword(updateUser.getPassword());

        userRepository.save(user);
    }

    public void deleteUser(Integer id) {
        User user = userRepository.findUserById(id);

        if (user == null) throw new ApiException("User not found");

        userRepository.delete(user);
    }
}