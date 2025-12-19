package com.example.ai_interviewer.controller;

import com.example.ai_interviewer.dto.MessageDto;
import com.example.ai_interviewer.dto.InterviewDto;
import com.example.ai_interviewer.dto.ResumeDto;
import com.example.ai_interviewer.exception.InterviewNotFoundException;
import com.example.ai_interviewer.exception.ResumeNotFoundException;
import com.example.ai_interviewer.exception.S3UploadException;
import com.example.ai_interviewer.model.Stage;
import com.example.ai_interviewer.service.InterviewService;
import com.example.ai_interviewer.service.ResumeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResumeControllerTests {
    @Autowired
    private TestRestTemplate testRestTemplate;
    @MockitoBean
    private ResumeService resumeService;
    @MockitoBean
    private InterviewService interviewService;

    @Test
    public void testCreateResume() throws S3UploadException, IOException {
        // arrange
        String fileName = "test-file.pdf";
        MockMultipartFile file = new MockMultipartFile(fileName, fileName, "application/pdf", "my test content".getBytes());

        ResumeDto expected = ResumeDto.builder()
                .id(5)
                .fileName(fileName)
                .build();
        when(resumeService.createResume(file)).thenReturn(expected);

        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("file", file.getBytes());
//        parameters.add("file", new org.springframework.core.io.ClassPathResource(dto.getFileName()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, headers);

        // act
        ResponseEntity<ResumeDto> response = testRestTemplate.exchange("/resumes", HttpMethod.POST, entity, ResumeDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(resumeService).createResume(any());
    }

    @Test
    public void testCreateResume_notPdf_statusBadRequest() {}

    @Test
    public void testCreateResume_s3Exception_status500() {}

    @Test
    public void testGetAllResumes() {
        // arrange
        ResumeDto resume = ResumeDto.builder()
                .id(1)
                .fileName("my_cool_file.pdf")
                .build();
        when(resumeService.getAllResumes()).thenReturn(List.of(resume));

        // act
        ResponseEntity<List<ResumeDto>> response = testRestTemplate.exchange("/resumes", HttpMethod.GET, null, new ParameterizedTypeReference<List<ResumeDto>>() {
        });

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().contains(resume)).isTrue();
        verify(resumeService).getAllResumes();
    }

    @Test
    public void testGetResume() throws IOException {
        // arrange
        Integer id = 6;

        String filePath = System.getProperty("user.dir") + "/Uploads/testing.pdf";
        File file = new File(filePath);
        byte[] fileContent = Files.readAllBytes(file.toPath());

        when(resumeService.getResumeBytes(id)).thenReturn(Optional.of(fileContent));

        InputStreamResource expected = new InputStreamResource(new ByteArrayInputStream(fileContent));

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}").build(Map.of("resumeId", id));
        ResponseEntity<InputStreamResource> response = testRestTemplate.getForEntity(url, InputStreamResource.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).isEqualTo(expected); // question: how to compare??
        verify(resumeService).getResumeBytes(id);
    }

    @Test
    public void testGetResume_resumeNotFound_statusNotFound() {
        // arrange
        Integer id = 6;
        when(resumeService.getResumeBytes(id)).thenReturn(Optional.empty());

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}").build(Map.of("resumeId", id));
        ResponseEntity<InputStreamResource> response = testRestTemplate.getForEntity(url, InputStreamResource.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(resumeService).getResumeBytes(id);
    }

    @Test
    public void testDeleteResume() {
        // arrange
        Integer id = 9;

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}").build(Map.of("resumeId", id));
        testRestTemplate.delete(url);

        // assert
        verify(resumeService).deleteResume(id);
    }

    @Test
    public void testCreateInterview() throws ResumeNotFoundException {
        // arrange
        Integer resumeId = 6;
        Integer interviewId = 16;
        Map<String, Object> dto = Map.of("jobDescription", "This is an accounting job.", "id", interviewId, "stage", "TEAM");
        InterviewDto sendToService = InterviewDto.builder()
                .id(null)
                .jobDescription("This is an accounting job.")
                .stage(Stage.TEAM)
                .build();
        InterviewDto expected = InterviewDto.builder()
                .id(1)
                .jobDescription("This is an accounting job.")
                .stage(Stage.PROJECTS)
                .build();

        when(interviewService.createInterview(resumeId, sendToService)).thenReturn(expected);

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews").build(Map.of("resumeId", resumeId));
        ResponseEntity<InterviewDto> response = testRestTemplate.postForEntity(url, dto, InterviewDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    public void testCreateInterview_resumeNotFound_statusNotFound() throws ResumeNotFoundException {
        // arrange
        Integer resumeId = 13;
        Map<String, Object> dto = Map.of("jobDescription", "This will be great!");
        InterviewDto toService = InterviewDto.builder()
                .jobDescription("This will be great!").build();
        when(interviewService.createInterview(resumeId, toService)).thenThrow(ResumeNotFoundException.class);

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews").build(Map.of("resumeId", resumeId));
        ResponseEntity<InterviewDto> response = testRestTemplate.postForEntity(url, dto, InterviewDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetAllInterviews() {
        // arrange
        Integer resumeId = 14;
        InterviewDto interview = InterviewDto.builder()
                .id(1)
                .jobDescription("This is an accounting job")
                .stage(Stage.TEAM)
                .build();
        when(interviewService.getAllInterviews(resumeId)).thenReturn(List.of(interview));

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews").build(Map.of("resumeId", resumeId));
        ResponseEntity<List<InterviewDto>> response = testRestTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<InterviewDto>>() {
        });

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().contains(interview)).isTrue();
        verify(interviewService).getAllInterviews(resumeId);
    }

    @Test // question: is it worth it to do these empty list tests?
    public void testGetAllInterviews_noInterviewsFound() {
        // arrange
        Integer resumeId = 17;
        when(interviewService.getAllInterviews(resumeId)).thenReturn(List.of());

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews").build(Map.of("resumeId", resumeId));
        ResponseEntity<List<InterviewDto>> response = testRestTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<InterviewDto>>() {
        });

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(interviewService).getAllInterviews(resumeId);
    }

    @Test
    public void testGetInterview() {
        // arrange
        Integer resumeId = 6;
        Integer interviewId = 12;
        InterviewDto interview = InterviewDto.builder()
                .id(interviewId)
                .jobDescription("This is a job for dental assistant")
                .stage(Stage.PROJECTS)
                .build();
        when(interviewService.getInterview(resumeId, interviewId)).thenReturn(Optional.of(interview));

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews/{interviewId}").build(Map.of("resumeId", resumeId, "interviewId", interviewId));
        ResponseEntity<InterviewDto> response = testRestTemplate.getForEntity(url, InterviewDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(interview);
        verify(interviewService).getInterview(resumeId, interviewId);
    }

    @Test
    public void testGetInterview_notFound_statusNotFound() {
        // arrange
        Integer resumeId = 13;
        Integer interviewId = 33;
        when(interviewService.getInterview(resumeId, interviewId)).thenReturn(Optional.empty());

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews/{interviewId}").build(Map.of("resumeId", resumeId, "interviewId", interviewId));
        ResponseEntity<InterviewDto> response = testRestTemplate.getForEntity(url, InterviewDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(interviewService).getInterview(resumeId, interviewId);
    }

    @Test
    public void testUpdateInterview() throws InterviewNotFoundException {
        // arrange
        Integer resumeId = 6;
        Integer interviewId = 12;
        Map<String, Object> interviewMap = Map.of(
                "jobDescription", "this is for actors only"
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(interviewMap);
        InterviewDto dto = InterviewDto.builder()
                .jobDescription("this is for actors only")
                .build();
        InterviewDto expected = InterviewDto.builder()
                .id(2)
                .jobDescription("this is for actors only")
                .stage(Stage.TEAM)
                .build();

        when(interviewService.updateInterviewJobDescription(resumeId, interviewId, dto)).thenReturn(expected);

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews/{interviewId}").build(Map.of("resumeId", resumeId, "interviewId", interviewId));
        ResponseEntity<InterviewDto> response = testRestTemplate.exchange(url, HttpMethod.PATCH, entity, InterviewDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    public void testUpdateInterview_throws_statusNotFound() throws InterviewNotFoundException {
        // arrange
        Integer resumeId = 17;
        Integer interviewId = 78;
        Map<String, Object> dtoMap = Map.of("jobDescription", "This is for the glory.");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(dtoMap);
        InterviewDto dto = InterviewDto.builder()
                .jobDescription("This is for the glory.")
                .build();
        when(interviewService.updateInterviewJobDescription(resumeId, interviewId, dto)).thenThrow(InterviewNotFoundException.class);

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews/{interviewId}").build(Map.of("resumeId", resumeId, "interviewId", interviewId));
        ResponseEntity<InterviewDto> response = testRestTemplate.exchange(url, HttpMethod.PATCH, entity, InterviewDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(interviewService).updateInterviewJobDescription(resumeId, interviewId, dto);
    }

    @Test
    public void testDeleteInterview() {
        // arrange
        Integer resumeId = 8;
        Integer interviewId = 81;

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews/{interviewId}").build(Map.of("resumeId", resumeId, "interviewId", interviewId));
        testRestTemplate.delete(url); // question: how can i check the httpstatus?

        // assert
        verify(interviewService).deleteInterview(resumeId, interviewId);
    }

    @Test
    public void testAddMessage() {
        // arrange
        Map<String, Object> message = Map.of(
                "input", "hello"
        );
        MessageDto expected = MessageDto.builder()
                .input("hello")
                .build();
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews/{interviewId}/messages").build(Map.of("resumeId", 1, "interviewId", 12));

        // act
        ResponseEntity<MessageDto> response = testRestTemplate.postForEntity(url, message, MessageDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    public void testGetMessages() {
        // arrange
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/interviews/{interviewId}/messages").build(Map.of("resumeId", 5, "interviewId", 10));

        // act
        ResponseEntity<List<MessageDto>> response = testRestTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<MessageDto>>() {
        });

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
