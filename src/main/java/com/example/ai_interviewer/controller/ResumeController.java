package com.example.ai_interviewer.controller;

import com.example.ai_interviewer.dto.MessageDto;
import com.example.ai_interviewer.dto.MockInterviewDto;
import com.example.ai_interviewer.dto.ResumeDto;
import com.example.ai_interviewer.model.Stage;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
public class ResumeController {

    @PostMapping("/resumes")
    public ResponseEntity<String> createResume(@RequestParam("file") MultipartFile file) {
        String filePath = System.getProperty("user.dir") + "/Uploads" + File.separator + file.getOriginalFilename();

        try {
            FileOutputStream fout = new FileOutputStream(filePath);
            fout.write(file.getBytes());
            fout.close();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error in uploading file", e);
        }

        ResumeDto resume = ResumeDto.builder()
                .id(1)
                .name(file.getOriginalFilename())
                .build();

        // question: why can't i return the dto?
        return new ResponseEntity<>(resume.getName(), HttpStatus.OK);
    }

    @GetMapping("/resumes")
    public ResponseEntity<List<ResumeDto>> getAllResumes() {
        String folderPath = System.getProperty("user.dir") + "/Uploads";
        File directory = new File(folderPath);
        String[] fileNames = directory.list();
        List<ResumeDto> resumes = new ArrayList<>();
        for (String name : fileNames) {
            ResumeDto resume = ResumeDto.builder()
                    .id(1)
                    .name(name)
                    .build();
            resumes.add(resume);
        }
        return new ResponseEntity<>(resumes, HttpStatus.OK);
    }

    @GetMapping("/resumes/{resumeId}")
    public ResponseEntity getResume(@PathVariable String resumeId) {
        String fileUploadPath = System.getProperty("user.dir") + "/Uploads";
        File directory = new File(fileUploadPath);
        String[] fileNames = directory.list();
        boolean contains = Arrays.asList(fileNames).contains(resumeId);
        if (!contains) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        String filePath = fileUploadPath + File.separator + resumeId;

        File file = new File(filePath);
        InputStreamResource resource = new InputStreamResource(new FileSystemResource(file));
        HttpHeaders headers = new HttpHeaders();
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }

    @DeleteMapping("/resumes/{resumeId}")
    public ResponseEntity<Void> deleteResume(@PathVariable Integer resumeId) {
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
