package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import com.nawaf.capstone3.Model.AiChatHistory;
import com.nawaf.capstone3.Repository.AiChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatHistoryService {

    private final AiChatHistoryRepository aiChatHistoryRepository;

    public List<AiChatHistory> getAllAiChatHistories(){
        List<AiChatHistory> aiChatHistories = aiChatHistoryRepository.findAll();

        if(aiChatHistories.isEmpty()) throw new ApiException("Ai chat histories not found");

        return aiChatHistories;
    }

    public AiChatHistory getAiChatHistory(Integer id){
        AiChatHistory aiChatHistory = aiChatHistoryRepository.findAiChatHistoriesById(id);

        if(aiChatHistory == null) throw new ApiException("Ai chat history not found");

        return aiChatHistory;
    }

    public void createAiChatHistory(AiChatHistory aiChatHistory){
        aiChatHistoryRepository.save(aiChatHistory);
    }

    public void updateAiChatHistory(Integer id, AiChatHistory aiChatHistory) {
        AiChatHistory oldAiChatHistory = getAiChatHistory(id);

        oldAiChatHistory.setUserMessage(aiChatHistory.getUserMessage());
        oldAiChatHistory.setAiResponse(aiChatHistory.getAiResponse());

        aiChatHistoryRepository.save(oldAiChatHistory);
    }

    public void deleteAiChatHistory(Integer id){
        AiChatHistory aiChatHistory = getAiChatHistory(id);

        aiChatHistoryRepository.delete(aiChatHistory);
    }


}
