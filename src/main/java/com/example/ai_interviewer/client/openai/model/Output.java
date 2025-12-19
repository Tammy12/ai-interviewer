package com.example.ai_interviewer.client.openai.model;

import lombok.Data;

import java.util.List;

@Data
public class Output {
    private String type;
    private String role;
    private List<Content> content;
}
