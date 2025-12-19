package com.example.ai_interviewer.client.openai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class InputFile {
    private final String type = "input_file";
    private String file_id;
}
