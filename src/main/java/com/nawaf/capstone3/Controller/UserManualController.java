package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.UserManual;
import com.nawaf.capstone3.Service.UserManualService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/userManual")
public class UserManualController {
    private final UserManualService userManualService;

    @GetMapping("/get")
    public ResponseEntity<?>getAll(){
        List<UserManual> userManualList=userManualService.getAll();
        return ResponseEntity.status(200).body(userManualList);
    }

    @PostMapping("/add")
    public ResponseEntity<?>addUserManual(@RequestBody UserManual userManual){
        userManualService.addUserManual(userManual);
        return ResponseEntity.status(200).body(new ApiResponse("add successfully"));
    }

    @PutMapping  ("/update/{userManualId}")
    public ResponseEntity<?>updateUserManual(@PathVariable Integer userManualId ,@RequestBody UserManual userManual){
        userManualService.updateUserManual(userManualId,userManual);
        return ResponseEntity.status(200).body(new ApiResponse("update successfully"));
    }
    @DeleteMapping("/delete/{userManualId}")
    public ResponseEntity<?>deleteUserManual(@PathVariable Integer userManualId){
        userManualService.deleteUserManual(userManualId);
        return ResponseEntity.status(200).body(new ApiResponse("deleted successfully"));
    }

}