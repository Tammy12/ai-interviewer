package com.example.ai_interviewer.dto;

import com.example.ai_interviewer.model.Stage;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class InterviewDto {
    private Integer id;
    @NotBlank
    private String jobDescription;
//    private List<MessageDto> messages; // question: should this be included??
    private Stage stage;
}
