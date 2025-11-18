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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@AllArgsConstructor
public class ResumeController {

    private ResumeService resumeService;
    private InterviewService interviewService;

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

    @PostMapping("/resumes/{resumeId}/interviews")
    public ResponseEntity<InterviewDto> createInterview(@PathVariable Integer resumeId, @RequestBody InterviewDto interview) {
        interview.setId(null); // shouldn't the id and the stage be set to null at the same place?
        try {
            InterviewDto newInterview = interviewService.createInterview(resumeId, interview);
            return new ResponseEntity<>(newInterview, HttpStatus.OK);
        } catch (ResumeNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/resumes/{resumeId}/interviews")
    public ResponseEntity<List<InterviewDto>> getAllInterviews(@PathVariable Integer resumeId) {
        List<InterviewDto> interviews = interviewService.getAllInterviews(resumeId);
        return new ResponseEntity<>(interviews, HttpStatus.OK);
    }

    @GetMapping("/resumes/{resumeId}/interviews/{interviewId}")
    public ResponseEntity<InterviewDto> getInterview(@PathVariable Integer resumeId, @PathVariable Integer interviewId) {
        Optional<InterviewDto> interviewOptional = interviewService.getInterview(resumeId, interviewId);
        if (interviewOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(interviewOptional.get(), HttpStatus.OK);
    }

    @PatchMapping("/resumes/{resumeId}/interviews/{interviewId}")
    public ResponseEntity<InterviewDto> updateInterview(@PathVariable Integer resumeId, @PathVariable Integer interviewId, @RequestBody InterviewDto interviewDto) {
        try {
            InterviewDto updated = interviewService.updateInterview(resumeId, interviewId, interviewDto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (InterviewNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @DeleteMapping("/resumes/{resumeId}/interviews/{interviewId}")
    public ResponseEntity<Void> deleteInterview(@PathVariable Integer resumeId, @PathVariable Integer interviewId) {
        interviewService.deleteInterview(resumeId, interviewId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/resumes/{resumeId}/interviews/{interviewId}/messages")
    public ResponseEntity<MessageDto> addMessage(@PathVariable Integer resumeId, @PathVariable Integer interviewId, @RequestBody MessageDto messageDto) {
        MessageDto message = MessageDto.builder()
                .id(1)
                .input(messageDto.getInput())
                .build();
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @GetMapping("/resumes/{resumeId}/interviews/{interviewId}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable Integer resumeId, @PathVariable Integer interviewId) {
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
    }
}
