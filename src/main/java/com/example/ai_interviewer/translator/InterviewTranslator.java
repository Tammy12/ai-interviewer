package com.example.ai_interviewer.translator;

import com.example.ai_interviewer.dto.InterviewDto;
import com.example.ai_interviewer.model.Interview;
import org.springframework.stereotype.Service;

@Service
public class InterviewTranslator {
    public Interview dtoToEntity(InterviewDto dto) {
        return Interview.builder()
                .id(dto.getId())
                .jobDescription(dto.getJobDescription())
                .stage(dto.getStage())
                .build();
    }

    public InterviewDto entityToDto(Interview entity) {
        return InterviewDto.builder()
                .id(entity.getId())
                .jobDescription(entity.getJobDescription())
                .stage(entity.getStage())
                .build();
    }
}
