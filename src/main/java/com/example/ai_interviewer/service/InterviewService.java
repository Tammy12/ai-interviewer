package com.example.ai_interviewer.service;

import com.example.ai_interviewer.dto.InterviewDto;
import com.example.ai_interviewer.exception.InterviewNotFoundException;
import com.example.ai_interviewer.exception.ResumeNotFoundException;
import com.example.ai_interviewer.model.Interview;
import com.example.ai_interviewer.model.Stage;
import com.example.ai_interviewer.repository.InterviewRepository;
import com.example.ai_interviewer.repository.ResumeRepository;
import com.example.ai_interviewer.translator.InterviewTranslator;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class InterviewService {
    private ResumeRepository resumeRepository;
    private InterviewRepository interviewRepository;
    private InterviewTranslator interviewTranslator;

    public InterviewDto createInterview(Integer resumeId, InterviewDto interviewDto) throws ResumeNotFoundException {
        if (resumeRepository.findById(resumeId).isEmpty()) {
            throw new ResumeNotFoundException();
        }

        Interview interview = interviewTranslator.dtoToEntity(interviewDto);
        interview.setResumeId(resumeId);
        interview.setStage(Stage.PROJECTS);
        Interview created = interviewRepository.save(interview);
        return interviewTranslator.entityToDto(created);
    }

    public List<InterviewDto> getAllInterviews(Integer resumeId) {
        List<Interview> interviews = interviewRepository.findAllByResumeId(resumeId);
        List<InterviewDto> results = new ArrayList<>();
        for (Interview interview : interviews) {
            results.add(interviewTranslator.entityToDto(interview));
        }
        return results;
    }

    public Optional<InterviewDto> getInterview(Integer resumeId, Integer interviewId) {
        return interviewRepository.findByResumeIdAndId(resumeId, interviewId)
                .map(interview -> interviewTranslator.entityToDto(interview));
    }

    public InterviewDto updateInterviewJobDescription(Integer resumeId, Integer interviewId, InterviewDto interviewDto) throws InterviewNotFoundException {
        Optional<Interview> interviewOptional = interviewRepository.findByResumeIdAndId(resumeId, interviewId);
        if (interviewOptional.isEmpty()) {
            throw new InterviewNotFoundException();
        }
        interviewOptional.get().setJobDescription(interviewDto.getJobDescription());
        Interview savedInterview = interviewRepository.save(interviewOptional.get());
        return interviewTranslator.entityToDto(savedInterview);
    }

    public void updateInterviewStage(Integer resumeId, Integer interviewId, Stage stage) throws InterviewNotFoundException {
        Optional<Interview> interviewOptional = interviewRepository.findByResumeIdAndId(resumeId, interviewId);
        if (interviewOptional.isEmpty()) {
            throw new InterviewNotFoundException();
        }
        interviewOptional.get().setStage(stage);
        interviewRepository.save(interviewOptional.get());
    }

    public void deleteInterview(Integer resumeId, Integer interviewId) {
        interviewRepository.findByResumeIdAndId(resumeId, interviewId)
                .ifPresent(interview -> interviewRepository.delete(interview));
    }
}
