package com.example.ai_interviewer.client.openai.model;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIResponse {
    private List<Output> output;
}
