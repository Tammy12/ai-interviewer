package com.example.ai_interviewer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "MOCK_INTERVIEW")
public class MockInterview {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "resume_id")
    private Integer resumeId;
    @Column(name = "job_description")
    private String jobDescription;
    @Column(name = "stage")
    private Stage stage;
}
