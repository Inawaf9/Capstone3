package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Model.User;
import com.nawaf.capstone3.Service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;


    // Get all users
    @GetMapping("/get")
    public ResponseEntity<?> getAllUser() {
        return ResponseEntity.status(200).body(userService.getAllUser());
    }


    // Add user
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@Valid @RequestBody User user) {
        userService.addUser(user);
        return ResponseEntity.status(200).body(" add successfully");
    }


    // Update user
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> update(
            @PathVariable Integer userId,
            @Valid @RequestBody User updateUser) {

        userService.update(userId, updateUser);
        return ResponseEntity.status(200).body("update successfully");
    }


    // Delete user
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> delete(@PathVariable Integer userId) {

        userService.delete(userId);
        return ResponseEntity.status(200).body("delete successfully");
    }
}