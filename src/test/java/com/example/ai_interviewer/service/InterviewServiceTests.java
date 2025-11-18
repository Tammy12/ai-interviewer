package com.example.ai_interviewer.service;

import com.example.ai_interviewer.dto.InterviewDto;
import com.example.ai_interviewer.exception.InterviewNotFoundException;
import com.example.ai_interviewer.exception.ResumeNotFoundException;
import com.example.ai_interviewer.model.Interview;
import com.example.ai_interviewer.model.Resume;
import com.example.ai_interviewer.model.Stage;
import com.example.ai_interviewer.repository.InterviewRepository;
import com.example.ai_interviewer.repository.ResumeRepository;
import com.example.ai_interviewer.translator.InterviewTranslator;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InterviewServiceTests {
    private ResumeRepository resumeRepository;
    private InterviewRepository interviewRepository;
    private InterviewTranslator interviewTranslator;
    private InterviewService interviewService;

    @BeforeEach
    public void BeforeEach() {
        resumeRepository = mock(ResumeRepository.class);
        interviewRepository = mock(InterviewRepository.class);
        interviewTranslator = new InterviewTranslator();
        interviewService = new InterviewService(resumeRepository, interviewRepository, interviewTranslator);
    }

    @Test
    public void testCreateInterview() throws ResumeNotFoundException {
        // arrange
        Integer resumeId = 2;
        InterviewDto dto = InterviewDto.builder()
                .jobDescription("here is a job!").build();

        Resume resume = Resume.builder()
                .id(resumeId)
                .fileName("my resume file.pdf").build();
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        Interview created = Interview.builder()
                .resumeId(resumeId)
                .jobDescription(dto.getJobDescription())
                .stage(Stage.PROJECTS).build();
        InterviewDto expected = InterviewDto.builder()
                .id(23)
                .jobDescription(created.getJobDescription())
                .stage(Stage.PROJECTS).build();
        when(interviewRepository.save(created)).thenReturn(created.toBuilder().id(23).build());

        // act
        InterviewDto result = interviewService.createInterview(resumeId, dto);

        // assert
        verify(resumeRepository).findById(resumeId);
        verify(interviewRepository).save(any());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testCreateInterview_resumeNotFound_throws() throws ResumeNotFoundException {
        // arrange
        Integer resumeId = 12;
        InterviewDto dto = InterviewDto.builder()
                .jobDescription("This is a really cool job")
                .build();
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.empty());

        // assert
        assertThatThrownBy(() -> interviewService.createInterview(resumeId, dto)).isInstanceOf(ResumeNotFoundException.class);
    }

    @Test
    public void testGetAllInterviews() {
        // arrange
        Integer resumeId = 23;
        Interview entity = Interview.builder()
                .id(56)
                .resumeId(5)
                .jobDescription("This job is better than your job.")
                .stage(Stage.COMPANY)
                .build();
        InterviewDto expected = InterviewDto.builder()
                .id(56)
                .jobDescription("This job is better than your job.")
                .stage(Stage.COMPANY)
                .build();
        when(interviewRepository.findAllByResumeId(resumeId)).thenReturn(List.of(entity));

        // act
        List<InterviewDto> result = interviewService.getAllInterviews(resumeId);

        // assert
        verify(interviewRepository).findAllByResumeId(resumeId);
        assertThat(result).containsOnly(expected);
    }

    @Test
    public void testGetAllInterviews_noInterviewsFound_returnsEmpty() {
        // arrange
        Integer resumeId = 13;
        when(interviewRepository.findAllByResumeId(resumeId)).thenReturn(List.of());

        // act
        List<InterviewDto> result = interviewService.getAllInterviews(resumeId);

        // assert
        verify(interviewRepository).findAllByResumeId(resumeId);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetInterview() {
        // arrange
        Integer resumeId = 15;
        Integer interviewId = 57;
        Interview entity = Interview.builder()
                .id(interviewId)
                .resumeId(resumeId)
                .jobDescription("This is a carpentry job.")
                .stage(Stage.TEAM)
                .build();
        InterviewDto expected = InterviewDto.builder()
                .id(interviewId)
                .jobDescription("This is a carpentry job.")
                .stage(Stage.TEAM)
                .build();
        when(interviewRepository.findByResumeIdAndId(resumeId, interviewId)).thenReturn(Optional.of(entity));

        // act
        Optional<InterviewDto> result = interviewService.getInterview(resumeId, interviewId);

        // assert
        verify(interviewRepository).findByResumeIdAndId(resumeId, interviewId);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
    }

    @Test
    public void testGetInterview_noInterviewFound_returnsEmptyOptional() {
        // arrange
        Integer resumeId = 13;
        Integer interviewId = 37;
        when(interviewRepository.findByResumeIdAndId(resumeId, interviewId)).thenReturn(Optional.empty());

        // act
        Optional<InterviewDto> result = interviewService.getInterview(resumeId, interviewId);

        // assert
        assertThat(result).isEmpty();
        verify(interviewRepository).findByResumeIdAndId(resumeId, interviewId);
    }

    @Test
    public void testUpdateInterview() throws InterviewNotFoundException {
        // arrange
        Integer resumeId = 16;
        Integer interviewId = 67;
        InterviewDto dto = InterviewDto.builder()
                .id(44)
                .jobDescription("This is my new job.")
                .stage(Stage.TECH)
                .build();

        Interview interview = Interview.builder()
                .id(interviewId)
                .resumeId(resumeId)
                .jobDescription("My old job")
                .stage(Stage.PROJECTS)
                .build();
        Interview toSave = interview.toBuilder().jobDescription("This is my new job.").build();
        InterviewDto expected = InterviewDto.builder()
                .id(interviewId)
                .jobDescription("This is my new job.")
                .stage(Stage.PROJECTS)
                .build();
        when(interviewRepository.findByResumeIdAndId(resumeId, interviewId)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(toSave)).thenReturn(toSave);

        // act
        InterviewDto result = interviewService.updateInterview(resumeId, interviewId, dto);

        // assert
        verify(interviewRepository).findByResumeIdAndId(resumeId, interviewId);
        verify(interviewRepository).save(any());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testUpdateInterview_interviewNotFound_throws() throws InterviewNotFoundException {
        // arrange
        Integer resumeId = 13;
        Integer interviewId = 37;
        InterviewDto dto = InterviewDto.builder()
                .jobDescription("This is the new job")
                .build();
        when(interviewRepository.findByResumeIdAndId(resumeId, interviewId)).thenReturn(Optional.empty());

        // assert
        assertThatThrownBy(() -> interviewService.updateInterview(resumeId, interviewId, dto)).isInstanceOf(InterviewNotFoundException.class);
        verify(interviewRepository).findByResumeIdAndId(resumeId, interviewId);
        verifyNoMoreInteractions(interviewRepository);
    }

    @Test
    public void testDeleteInterview() {
        // arrange
        Integer resumeId = 12;
        Integer interviewId = 22;
        Interview interview = Interview.builder()
                .id(interviewId)
                .resumeId(resumeId)
                .jobDescription("Here it is")
                .stage(Stage.PROJECTS).build();
        when(interviewRepository.findByResumeIdAndId(resumeId, interviewId)).thenReturn(Optional.of(interview));

        // act
        interviewService.deleteInterview(resumeId, interviewId);

        // assert
        verify(interviewRepository).findByResumeIdAndId(resumeId, interviewId);
        verify(interviewRepository).delete(interview);
    }

    @Test
    public void testDeleteInterview_interviewNotFound() {
        // arrange
        Integer resumeId = 17;
        Integer interviewId = 77;
        when(interviewRepository.findByResumeIdAndId(resumeId, interviewId)).thenReturn(Optional.empty());

        // act
        interviewService.deleteInterview(resumeId, interviewId);

        // assert
        verify(interviewRepository).findByResumeIdAndId(resumeId, interviewId);
        verifyNoMoreInteractions(interviewRepository);
    }
}
