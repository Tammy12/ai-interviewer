package com.example.ai_interviewer.controller;

import com.example.ai_interviewer.dto.MessageDto;
import com.example.ai_interviewer.dto.MockInterviewDto;
import com.example.ai_interviewer.dto.ResumeDto;
import com.example.ai_interviewer.model.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResumeControllerTests {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testCreateResume() {
        // arrange
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("file", new org.springframework.core.io.ClassPathResource("test-file.txt"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, headers);

        // act
        ResponseEntity<String> response = testRestTemplate.exchange("/resumes", HttpMethod.POST, entity, String.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("test-file.txt");
    }

    @Test
    public void testGetAllResumes() {
        // arrange
        ResumeDto resume = ResumeDto.builder()
                .id(1)
                .name("test-scp.txt")
                .build();

        // act
        ResponseEntity<List<ResumeDto>> response = testRestTemplate.exchange("/resumes", HttpMethod.GET, null, new ParameterizedTypeReference<List<ResumeDto>>() {
        });

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().contains(resume)).isTrue();
    }

    @Test
    public void testGetResume() {
        // arrange

        // act
//        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}").build(Map.of("resumeId", ))
//        testRestTemplate.getForEntity("/resumes")

        // assert
    }

    @Test
    public void testDeleteResume() {
        // arrange
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}").build(Map.of("resumeId", 9));

        // act
    }

    @Test
    public void testCreateMockInterview() {
        // arrange
        MockInterviewDto mockInterviewDto = MockInterviewDto.builder()
                .jobDescription("This is an accounting job.")
                .build();

        MockInterviewDto expected = mockInterviewDto.toBuilder()
                .id(1)
                .stage(Stage.PROJECTS)
                .build();

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/mock-interviews").build(Map.of("resumeId", 6));
        ResponseEntity<MockInterviewDto> response = testRestTemplate.postForEntity(url, mockInterviewDto, MockInterviewDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    public void testGetAllMockInterviews() {
        // arrange
        MockInterviewDto interview = MockInterviewDto.builder()
                .id(1)
                .jobDescription("This is an accounting job")
                .stage(Stage.TEAM)
                .build();

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/mock-interviews").build(Map.of("resumeId", 6));
        ResponseEntity<List<MockInterviewDto>> response = testRestTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<MockInterviewDto>>() {
        });

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().contains(interview)).isTrue();
    }

    @Test
    public void testGetMockInterview() {
        // arrange
        MockInterviewDto interview = MockInterviewDto.builder()
                .id(1)
                .jobDescription("This is a job for dental assistant")
                .stage(Stage.PROJECTS)
                .build();

        // act
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/mock-interviews/{interviewId}").build(Map.of("resumeId", 6, "interviewId", 12));
        ResponseEntity<MockInterviewDto> response = testRestTemplate.getForEntity(url, MockInterviewDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(interview);
    }

    @Test
    public void testUpdateMockInterview() {
        // arrange
        Map<String, Object> interview = Map.of(
                "jobDescription", "this is for actors only"
        );

        MockInterviewDto expected = MockInterviewDto.builder()
                .id(2)
                .jobDescription("this is for actors only")
                .stage(Stage.TEAM)
                .build();

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(interview);

        // act

        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/mock-interviews/{interviewId}").build(Map.of("resumeId", 6, "interviewId", 12));
        ResponseEntity<MockInterviewDto> response = testRestTemplate.exchange(url, HttpMethod.PATCH, entity, MockInterviewDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    public void testAddMessage() {
        // arrange
        Map<String, Object> message = Map.of(
                "input", "hello"
        );
        MessageDto expected = MessageDto.builder()
                .id(1)
                .input("hello")
                .build();
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/mock-interviews/{interviewId}/messages").build(Map.of("resumeId", 1, "interviewId", 12));

        // act
        ResponseEntity<MessageDto> response = testRestTemplate.postForEntity(url, message, MessageDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    public void testGetMessages() {
        // arrange
        URI url = UriComponentsBuilder.fromPath("/resumes/{resumeId}/mock-interviews/{interviewId}/messages").build(Map.of("resumeId", 5, "interviewId", 10));

        // act
        ResponseEntity<List<MessageDto>> response = testRestTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<MessageDto>>() {
        });

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
