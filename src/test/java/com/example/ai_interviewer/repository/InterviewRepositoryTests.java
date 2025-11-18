package com.example.ai_interviewer.repository;

import com.example.ai_interviewer.model.Interview;
import com.example.ai_interviewer.model.Resume;
import com.example.ai_interviewer.model.Stage;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class InterviewRepositoryTests {
    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private InterviewRepository interviewRepository;

    @AfterEach
    public void afterEach() {
        // because we use cascade delete, this deletes both resumes and interivews
        resumeRepository.deleteAll();
    }

    @Test
    public void testSave_noResumeId_throws() {
        // arrange
        Interview interview = Interview.builder()
                .jobDescription("this is for a janitorial position")
                .build();

        // act
        assertThatThrownBy(() -> interviewRepository.save(interview)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testSave_newInterviewGivenId_throws() {
        // arrange
        Resume resume = Resume.builder()
                .fileName("myfile.pdf").build();
        Resume savedResume = resumeRepository.save(resume);
        Interview interview = Interview.builder()
                .id(5)
                .resumeId(savedResume.getId())
                .jobDescription("this is your job now")
                .stage(Stage.PROJECTS)
                .build();

        // act
        assertThatThrownBy(() -> interviewRepository.save(interview)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    public void testFindAllByResumeId() {
        // arrange
        Resume resume1 = Resume.builder()
                .fileName("file 1.pdf").build();
        Resume resume2 = Resume.builder()
                .fileName("file num2.pdf").build();
        resume1 = resumeRepository.save(resume1);
        resume2 = resumeRepository.save(resume2);
        Interview interview1 = Interview.builder()
                .resumeId(resume1.getId())
                .jobDescription("this is a job")
                .stage(Stage.PROJECTS)
                .build();
        Interview interview2 = Interview.builder()
                .resumeId(resume2.getId())
                .jobDescription("this is not a job")
                .stage(Stage.PROJECTS)
                .build();
        Interview interview3 = Interview.builder()
                .resumeId(resume2.getId())
                .jobDescription("what is a job")
                .stage(Stage.PROJECTS)
                .build();
        interviewRepository.save(interview1);
        interviewRepository.save(interview2);
        interviewRepository.save(interview3);

        // act
        List<Interview> results = interviewRepository.findAllByResumeId(resume2.getId());

        // assert
        assertThat(results).size().isEqualTo(2);
        assertThat(results).contains(interview2);
        assertThat(results).contains(interview3);
    }

    @Test
    public void testFindAllByResumeId_resumeIdNotFound_returnsEmpty() {
        // act
        List<Interview> results = interviewRepository.findAllByResumeId(12);

        // assert
        assertThat(results).isEmpty();
    }

    @Test
    public void testFindByResumeIdAndId() {
        // arrange
        Resume resume = Resume.builder()
                .fileName("myfilewhoo.pdf")
                .build();
        resume = resumeRepository.save(resume);
        Interview interview = Interview.builder()
                .resumeId(resume.getId())
                .jobDescription("This is a temp job")
                .stage(Stage.PROJECTS)
                .build();
        interview = interviewRepository.save(interview);

        // act
        Optional<Interview> result = interviewRepository.findByResumeIdAndId(resume.getId(), interview.getId());

        // assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(interview);
    }

    @Test
    public void testFindByResumeIdAndId_missingIds_returnsEmpty() {
        // arrange
        Resume resume = Resume.builder()
                .fileName("our file.pdf")
                .build();
        resume = resumeRepository.save(resume);
        Interview interview = Interview.builder()
                .resumeId(resume.getId())
                .jobDescription("this is a hard job")
                .stage(Stage.PROJECTS)
                .build();

        // act
        Optional<Interview> missingInterviewId = interviewRepository.findByResumeIdAndId(resume.getId(), 145);
        Optional<Interview> missingResumeId = interviewRepository.findByResumeIdAndId(500, interview.getId());
        Optional<Interview> bothMissing = interviewRepository.findByResumeIdAndId(421, 562);

        // assert
        assertThat(missingInterviewId).isNotPresent();
        assertThat(missingResumeId).isNotPresent();
        assertThat(bothMissing).isNotPresent();
    }
}
