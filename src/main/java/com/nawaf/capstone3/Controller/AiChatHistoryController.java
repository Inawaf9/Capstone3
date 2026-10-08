package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.AiChatHistory;
import com.nawaf.capstone3.Service.AiChatHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai-chat-history")
@RequiredArgsConstructor
public class AiChatHistoryController {

    private final AiChatHistoryService aiChatHistoryService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAiChatHistories() {
        return ResponseEntity.status(200).body(aiChatHistoryService.getAiChatHistories());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getAiChatHistoryById(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(aiChatHistoryService.getAiChatHistoryById(id));
    }

    @PostMapping("/add/{vehicleId}")
    public ResponseEntity<?> addAiChatHistory(@PathVariable Integer vehicleId, @Valid @RequestBody AiChatHistory aiChatHistory) {
        aiChatHistoryService.addAiChatHistory(vehicleId, aiChatHistory);
        return ResponseEntity.status(201).body(new ApiResponse("AI chat history added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAiChatHistory(@PathVariable Integer id, @Valid @RequestBody AiChatHistory aiChatHistory) {
        aiChatHistoryService.updateAiChatHistory(id, aiChatHistory);
        return ResponseEntity.status(200).body(new ApiResponse("AI chat history updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAiChatHistory(@PathVariable Integer id) {
        aiChatHistoryService.deleteAiChatHistory(id);
        return ResponseEntity.status(200).body(new ApiResponse("AI chat history deleted successfully"));
    }
}
