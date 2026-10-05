package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Api.ApiResponse;
import com.nawaf.capstone3.Model.AiChatHistory;
import com.nawaf.capstone3.Service.AiChatHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/ai-chat-history")
@RequiredArgsConstructor
public class AiChatHistoryController {

    private final AiChatHistoryService aiChatHistoryService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllAiChatHistories(){
        return ResponseEntity.status(200).body(aiChatHistoryService.getAllAiChatHistories());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getAiChatHistory(@PathVariable Integer id){
        return ResponseEntity.status(200).body(aiChatHistoryService.getAiChatHistory(id));
    }

    @PostMapping("/add")
    public ResponseEntity<?> createAiChatHistory(@Valid @RequestBody AiChatHistory aiChatHistory){
        aiChatHistoryService.createAiChatHistory(aiChatHistory);

        return ResponseEntity.status(201).body(new ApiResponse("Create ai chat history successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAiChatHistory(@PathVariable Integer id, @Valid @RequestBody AiChatHistory aiChatHistory){
        aiChatHistoryService.updateAiChatHistory(id, aiChatHistory);

        return ResponseEntity.status(200).body(new ApiResponse("Update ai chat history successfully"));
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<?> deleteAiChatHistory(@PathVariable Integer id){
        aiChatHistoryService.deleteAiChatHistory(id);

        return ResponseEntity.status(200).body(new ApiResponse("Delete ai chat history successfully"));
    }
}
