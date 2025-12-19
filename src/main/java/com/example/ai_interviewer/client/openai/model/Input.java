package com.example.ai_interviewer.client.openai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Input {
    private String role;
    private Object content;
}
