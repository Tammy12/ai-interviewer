package com.example.ai_interviewer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "RESUME")
public class Resume {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "url")
    private String url;
}
