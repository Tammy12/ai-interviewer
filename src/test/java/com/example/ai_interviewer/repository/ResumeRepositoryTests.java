package com.example.ai_interviewer.repository;

import com.example.ai_interviewer.model.Interview;
import com.example.ai_interviewer.model.Resume;
import com.example.ai_interviewer.model.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class ResumeRepositoryTests {
    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private InterviewRepository interviewRepository;

    @Test
    public void testSave_newResumeNoGivenId_createsId() {
        // arrange
        String name = "my_file.pdf";
        Resume entity = Resume.builder()
                .fileName(name)
                .build();

        // act
        Resume created = resumeRepository.save(entity);

        // assert
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getFileName()).isEqualTo(name);
    }

    @Test
    public void testSave_newResumeGivenId_throws() {
        // arrange
        Resume entity = Resume.builder()
                .id(199)
                .fileName("my_file.pdf")
                .build();

        // act
        assertThatThrownBy(() -> resumeRepository.save(entity)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    public void testDelete() {
        // arrange
        Resume entity = Resume.builder()
                .fileName("my_file.pdf")
                .build();
        Resume created = resumeRepository.save(entity);
        Integer id = created.getId();

        // act
        resumeRepository.delete(created);

        // assert
        Optional<Resume> found = resumeRepository.findById(id);
        assertThat(found).isNotPresent();
    }

    @Test
    public void testDelete_cascadingDelete() {
        // arrange
        Resume resumeEntity = Resume.builder()
                .fileName("my_file.pdf")
                .build();
        Resume resume = resumeRepository.save(resumeEntity);

        Interview interviewEntity = Interview.builder()
                .resumeId(resume.getId())
                .jobDescription("This is an accounting job.")
                .stage(Stage.PROJECTS)
                .build();
        Interview interview = interviewRepository.save(interviewEntity);

        // act
        resumeRepository.delete(resume);

        // assert
        Optional<Interview> result = interviewRepository.findById(interview.getId());
        assertThat(result).isNotPresent();
    }
}
