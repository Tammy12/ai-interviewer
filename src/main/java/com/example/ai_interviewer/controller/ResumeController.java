package com.example.ai_interviewer.controller;

import com.example.ai_interviewer.dto.MessageDto;
import com.example.ai_interviewer.dto.MockInterviewDto;
import com.example.ai_interviewer.dto.ResumeDto;
import com.example.ai_interviewer.exception.S3UploadException;
import com.example.ai_interviewer.model.Stage;
import com.example.ai_interviewer.service.FileService;
import com.example.ai_interviewer.service.ResumeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@AllArgsConstructor
public class ResumeController {

    private ResumeService resumeService;

    @PostMapping("/resumes")
    public ResponseEntity<ResumeDto> createResume(@RequestParam("file") MultipartFile file) {
        try {
            ResumeDto resume = resumeService.createResume(file);
            if (!file.getContentType().equals("application/pdf")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed.");
            }
            return new ResponseEntity<>(resume, HttpStatus.OK);
        } catch (S3UploadException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading file.");
        }
    }

    @GetMapping("/resumes")
    public ResponseEntity<List<ResumeDto>> getResumeList() {
        List<ResumeDto> allResumes = resumeService.getAllResumes();
        return new ResponseEntity<>(allResumes, HttpStatus.OK);
    }

    @GetMapping("/resumes/{resumeId}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getResume(@PathVariable Integer resumeId) {
        MediaType contentType = MediaType.APPLICATION_PDF;
        Optional<byte[]> dataContainer = resumeService.getResume(resumeId);
        if (dataContainer.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(dataContainer.get()));
        return ResponseEntity.ok()
                .contentType(contentType)
                .body(resource);
    }

    @DeleteMapping("/resumes/{resumeId}")
    public ResponseEntity<Void> deleteResume(@PathVariable Integer resumeId) {
        resumeService.deleteResume(resumeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/resumes/{resumeId}/mock-interviews")
    public ResponseEntity<MockInterviewDto> createMockInterview(@PathVariable Integer resumeId, @RequestBody MockInterviewDto mockInterview) {
        if (mockInterview.getJobDescription() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job description is required.");
        }

        MockInterviewDto interview = MockInterviewDto.builder()
                .id(1)
                .jobDescription(mockInterview.getJobDescription())
                .stage(Stage.PROJECTS)
                .build();

        return new ResponseEntity<>(interview, HttpStatus.OK);
    }

    @GetMapping("/resumes/{resumeId}/mock-interviews")
    public ResponseEntity<List<MockInterviewDto>> getAllMockInterviews(@PathVariable Integer resumeId) {
        MockInterviewDto interview = MockInterviewDto.builder()
                .id(1)
                .jobDescription("This is an accounting job")
                .stage(Stage.TEAM)
                .build();
        return new ResponseEntity<>(List.of(interview), HttpStatus.OK);
    }

    @GetMapping("/resumes/{resumeId}/mock-interviews/{interviewId}")
    public ResponseEntity<MockInterviewDto> getMockInterview(@PathVariable Integer resumeId, @PathVariable Integer interviewId) {
        MockInterviewDto interview = MockInterviewDto.builder()
                .id(1)
                .jobDescription("This is a job for dental assistant")
                .stage(Stage.PROJECTS)
                .build();
        return new ResponseEntity<>(interview, HttpStatus.OK);
    }

    @PatchMapping("/resumes/{resumeId}/mock-interviews/{interviewId}")
    public ResponseEntity<MockInterviewDto> updateMockInterview(@PathVariable Integer resumeId, @PathVariable Integer interviewId, @RequestBody MockInterviewDto mockInterviewDto) {
        MockInterviewDto interview = MockInterviewDto.builder()
                .id(2)
                .jobDescription(mockInterviewDto.getJobDescription())
                .stage(Stage.TEAM)
                .build();
        return new ResponseEntity<>(interview, HttpStatus.OK);
    }

    @DeleteMapping("/resumes/{resumeId}/mock-interviews/{interviewId}")
    public ResponseEntity<Void> deleteMockInterview() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/resumes/{resumeId}/mock-interviews/{interviewId}/messages")
    public ResponseEntity<MessageDto> addMessage(@PathVariable Integer resumeId, @PathVariable Integer interviewId, @RequestBody MessageDto messageDto) {
        MessageDto message = MessageDto.builder()
                .id(1)
                .input(messageDto.getInput())
                .build();
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @GetMapping("/resumes/{resumeId}/mock-interviews/{interviewId}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable Integer resumeId, @PathVariable Integer interviewId) {
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
    }
}
