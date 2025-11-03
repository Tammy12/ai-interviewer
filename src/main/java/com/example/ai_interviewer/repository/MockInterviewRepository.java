package com.example.ai_interviewer.repository;

import com.example.ai_interviewer.model.MockInterview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MockInterviewRepository extends JpaRepository<MockInterview, Integer> {
}
