package com.example.ai_interviewer.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder(toBuilder = true)
public class ResumeDto {
    private Integer id;
    private String name;
}
