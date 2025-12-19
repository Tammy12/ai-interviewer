package com.example.ai_interviewer.repository;

import com.example.ai_interviewer.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Integer> {
}
