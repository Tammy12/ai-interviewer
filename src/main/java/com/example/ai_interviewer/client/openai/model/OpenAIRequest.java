package com.example.ai_interviewer.client.openai.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class OpenAIRequest {
    private String model;
    private List<Input> input;
}
