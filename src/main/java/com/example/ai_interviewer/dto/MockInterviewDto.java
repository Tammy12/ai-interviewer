package com.example.ai_interviewer.dto;

import com.example.ai_interviewer.model.Stage;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder(toBuilder = true)
@Data
public class MockInterviewDto {
    private Integer id;
    private String jobDescription;
//    private List<MessageDto> messages; // question: should this be included??
    private Stage stage;
}
