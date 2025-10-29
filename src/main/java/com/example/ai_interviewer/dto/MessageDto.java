package com.example.ai_interviewer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class MessageDto {
    private Integer id;
    private String instructions;
    private String input;
}
