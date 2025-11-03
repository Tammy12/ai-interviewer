package com.example.ai_interviewer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "MESSAGE")
public class Message {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "mock_interview_id")
    private Integer mockInterviewId;
    @Column(name = "instructions")
    private String instructions;
    @Column(name = "input")
    private String input;
}
