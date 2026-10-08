package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.User;
import com.nawaf.capstone3.DTO.UserRequest;
import org.springframework.transaction.annotation.Transactional;
import com.nawaf.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
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

    public void addUser(UserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        if (request.password() == null || request.password().isBlank()) throw new ApiException("Password is required");
        user.setPassword(request.password());
        User emailUser = userRepository.findUserByEmail(user.getEmail());
        User phoneUser = userRepository.findUserByPhoneNumber(user.getPhoneNumber());

        if (emailUser != null) throw new ApiException("Email already exists");
        if (phoneUser != null) throw new ApiException("Phone number already exists");

        userRepository.save(user);
    }

    public void updateUser(Integer id, UserRequest updateUser) {
        User user = userRepository.findUserById(id);

        if (user == null) throw new ApiException("User not found");

        User emailUser = userRepository.findUserByEmail(updateUser.email());
        User phoneUser = userRepository.findUserByPhoneNumber(updateUser.phoneNumber());

        if (emailUser != null && !emailUser.getId().equals(id)) throw new ApiException("Email already exists");
        if (phoneUser != null && !phoneUser.getId().equals(id)) throw new ApiException("Phone number already exists");

        user.setName(updateUser.name());
        user.setEmail(updateUser.email());
        user.setPhoneNumber(updateUser.phoneNumber());
        if (updateUser.password() != null) user.setPassword(updateUser.password());

        userRepository.save(user);
    }

    public void deleteUser(Integer id) {
        User user = userRepository.findUserById(id);

        if (user == null) throw new ApiException("User not found");

        if (!user.getVehicles().isEmpty() || !user.getNotifications().isEmpty())
            throw new ApiException("User has vehicle or notification history; delete dependent records explicitly first");
        userRepository.delete(user);
    }
}
