package com.example.ai_interviewer.service;

import com.example.ai_interviewer.dto.ResumeDto;
import com.example.ai_interviewer.exception.S3UploadException;
import com.example.ai_interviewer.model.Resume;
import com.example.ai_interviewer.repository.ResumeRepository;
import com.example.ai_interviewer.translator.ResumeTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

// question: why not @ExtendWith(MockitoExtension.class)?
public class ResumeServiceTests {
    private S3FileService s3FileService;
    private ResumeRepository resumeRepository;
    private ResumeTranslator resumeTranslator;
    private ResumeService resumeService;

    @BeforeEach
    public void BeforeEach() {
        s3FileService = mock(S3FileService.class);
        resumeRepository = mock(ResumeRepository.class);
        resumeTranslator = new ResumeTranslator();
        resumeService = new ResumeService(s3FileService, resumeRepository, resumeTranslator);
    }

    @Test
    public void testCreateResume() throws S3UploadException {
        // arrange
        String fileName = "file.pdf";
        MockMultipartFile file = new MockMultipartFile(fileName, fileName, "application/pdf", "Here is my data".getBytes());
        Resume resume = Resume.builder()
                .fileName(fileName)
                .build();
        Resume savedResume = resume.toBuilder().id(6).build();
        ResumeDto expected = ResumeDto.builder()
                .id(6)
                .fileName(fileName)
                .build();
        when(resumeRepository.save(resume)).thenReturn(savedResume);

        // act
        ResumeDto result = resumeService.createResume(file);

        // assert
        assertThat(result).isEqualTo(expected);
        verify(resumeRepository).save(resume);
        verify(s3FileService).uploadFile(file, "6");
        verify(resumeRepository, never()).delete(any());
    }

    @Test
    public void testCreateResume_throws() throws S3UploadException {
        // arrange
        String fileName = "myfile.pdf";
        Resume resume = Resume.builder()
                .fileName(fileName)
                .build();
        Resume savedResume = resume.toBuilder().id(12).build();
        when(resumeRepository.save(resume)).thenReturn(savedResume);
        MockMultipartFile file = new MockMultipartFile(fileName, fileName, "application/pdf", "data is here!".getBytes());
        doThrow(new S3UploadException("This is wrong!"))
                .when(s3FileService)
                .uploadFile(file, savedResume.getId().toString());

        // assert
        assertThatThrownBy(() -> resumeService.createResume(file)).isInstanceOf(S3UploadException.class);
        verify(resumeRepository).delete(savedResume);
    }

    @Test
    public void testGetAllResumes() {
        // arrange
        Resume resume = Resume.builder()
                .id(5)
                .fileName("my_file.pdf")
                .build();
        ResumeDto expected = ResumeDto.builder()
                .id(5)
                .fileName("my_file.pdf")
                .build();
        when(resumeRepository.findAll()).thenReturn(List.of(resume));

        // act
        List<ResumeDto> result = resumeService.getAllResumes();

        // assert
        verify(resumeRepository).findAll();
        assertThat(result).containsOnly(expected);
    }

    @Test
    public void testGetResumeBytes() {
        // arrange
        Integer id = 12;
        String fileName = "mytestfilename";
        Resume resume = Resume.builder()
                .id(id)
                .fileName(fileName)
                .build();
        when(resumeRepository.findById(id)).thenReturn(Optional.of(resume));
        byte[] bytes = HexFormat.of().parseHex("3a0f45db");
        when(s3FileService.retrieveFile(id.toString(), fileName)).thenReturn(bytes);

        // act
        Optional<byte[]> result = resumeService.getResumeBytes(id);

        // assert
        verify(resumeRepository).findById(id);
        verify(s3FileService).retrieveFile(id.toString(), fileName);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(bytes);
    }

    @Test
    public void testGetResumeBytes_resumeIdNotFound_returnsEmptyOptional() {
        // arrange
        Integer id = 17;
        when(resumeRepository.findById(id)).thenReturn(Optional.empty());

        // act
        Optional<byte[]> result = resumeService.getResumeBytes(id);

        // assert
        verify(resumeRepository).findById(id);
        verify(s3FileService, never()).retrieveFile(any(), any());
        assertThat(result).isNotPresent();
    }

    @Test
    public void testDeleteResume() {
        // arrange
        Integer id = 12;
        String fileName = "myfile.pdf";
        Resume resume = Resume.builder()
                .id(id)
                .fileName(fileName)
                .build();
        when(resumeRepository.findById(id)).thenReturn(Optional.of(resume));

        // act
        resumeService.deleteResume(id);

        // assert
        verify(resumeRepository).findById(id);
        verify(s3FileService).deleteFile(id.toString(), fileName);
        verify(resumeRepository).delete(resume);
    }

    @Test
    public void testDeleteResume_resumeIdNotFound() {
        // arrange
        Integer id = 15;
        when(resumeRepository.findById(id)).thenReturn(Optional.empty());

        // act
        resumeService.deleteResume(id);

        // assert
        verifyNoInteractions(s3FileService);
        verify(resumeRepository).findById(id);
        verifyNoMoreInteractions(resumeRepository);
    }
}
