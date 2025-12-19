package com.example.ai_interviewer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class MessageDto {
    private String role;
    @NotBlank
    private String input;
}
