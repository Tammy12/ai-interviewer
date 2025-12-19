package com.example.ai_interviewer.translator;

import com.example.ai_interviewer.dto.ResumeDto;
import com.example.ai_interviewer.model.Resume;
import org.springframework.stereotype.Service;

@Service
public class ResumeTranslator {
    public Resume dtoToEntity(ResumeDto dto) {
        return Resume.builder()
                .id(dto.getId())
                .fileName(dto.getFileName())
                .build();
    }

    public ResumeDto entityToDto(Resume entity) {
        return ResumeDto.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .build();
    }
}
