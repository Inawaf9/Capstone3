package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.AiChatHistory;
import com.nawaf.capstone3.Model.Vehicle;
import com.nawaf.capstone3.Repository.AiChatHistoryRepository;
import com.nawaf.capstone3.Repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatHistoryService {

    private final AiChatHistoryRepository aiChatHistoryRepository;
    private final VehicleRepository vehicleRepository;

    public List<AiChatHistory> getAiChatHistories() {
        return aiChatHistoryRepository.findAll();
    }

    public AiChatHistory getAiChatHistoryById(Integer id) {
        AiChatHistory aiChatHistory = aiChatHistoryRepository.findAiChatHistoryById(id);

        if (aiChatHistory == null) throw new ApiException("AI chat history not found");

        return aiChatHistory;
    }

    public void addAiChatHistory(Integer vehicleId, AiChatHistory aiChatHistory) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId);

        if (vehicle == null) throw new ApiException("Vehicle not found");

        aiChatHistory.setVehicle(vehicle);

        aiChatHistoryRepository.save(aiChatHistory);
    }

    public void updateAiChatHistory(Integer id, AiChatHistory updateAiChatHistory) {
        AiChatHistory aiChatHistory = aiChatHistoryRepository.findAiChatHistoryById(id);

        if (aiChatHistory == null) throw new ApiException("AI chat history not found");

        aiChatHistory.setUserMessage(updateAiChatHistory.getUserMessage());
        aiChatHistory.setAiResponse(updateAiChatHistory.getAiResponse());

        aiChatHistoryRepository.save(aiChatHistory);
    }

    public void deleteAiChatHistory(Integer id) {
        AiChatHistory aiChatHistory = aiChatHistoryRepository.findAiChatHistoryById(id);

        if (aiChatHistory == null) throw new ApiException("AI chat history not found");

        aiChatHistoryRepository.delete(aiChatHistory);
    }
}