package com.example.ai_interviewer.repository;

import com.example.ai_interviewer.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Integer> {
}
