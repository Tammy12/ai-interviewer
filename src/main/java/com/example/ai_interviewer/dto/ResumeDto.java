package com.example.ai_interviewer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ResumeDto {
    private Integer id;
    private String fileName;
}
