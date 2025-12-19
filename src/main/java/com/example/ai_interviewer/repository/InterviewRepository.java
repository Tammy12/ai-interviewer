package com.example.ai_interviewer.repository;

import com.example.ai_interviewer.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Integer> {
    List<Interview> findAllByResumeId(Integer resumeId);

    Optional<Interview> findByResumeIdAndId(Integer resumeId, Integer id);
}
