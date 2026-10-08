package com.nawaf.capstone3.Repository;

import com.nawaf.capstone3.Model.AiChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Integer> {

    AiChatHistory findAiChatHistoryById(Integer id);
}
