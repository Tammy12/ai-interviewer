package com.example.ai_interviewer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "INTERVIEW")
public class Interview {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "resume_id")
    private Integer resumeId;
    @Column(name = "job_description")
    private String jobDescription;
    @Column(name = "stage")
    private Stage stage;
}
