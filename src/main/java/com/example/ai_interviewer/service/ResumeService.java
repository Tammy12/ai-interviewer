package com.example.ai_interviewer.service;

import com.example.ai_interviewer.dto.ResumeDto;
import com.example.ai_interviewer.exception.S3UploadException;
import com.example.ai_interviewer.model.Resume;
import com.example.ai_interviewer.repository.ResumeRepository;
import com.example.ai_interviewer.translator.ResumeTranslator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ResumeService {

    private FileService fileService;
    private ResumeRepository resumeRepository;
    private ResumeTranslator resumeTranslator;

    public ResumeDto createResume(MultipartFile file) throws S3UploadException {
        ResumeDto resumeDto = ResumeDto.builder()
            .fileName(file.getOriginalFilename())
            .build();

        Resume resumeEntity = resumeTranslator.DtoToEntity(resumeDto);

        Resume resume = resumeRepository.save(resumeEntity);
        try {
            fileService.uploadFile(file, resume.getId().toString());
        } catch (S3UploadException e) {
            resumeRepository.delete(resume);
            throw e;
        }

        return resumeTranslator.EntityToDto(resume);
    }

    public List<ResumeDto> getAllResumes() {
        List<Resume> resumes = resumeRepository.findAll();
        List<ResumeDto> dtos = new ArrayList<>();
        for (Resume resume : resumes) {
            dtos.add(resumeTranslator.EntityToDto(resume));
        }
        return dtos;
    }

    public Optional<byte[]> getResume(Integer resumeId) {
        Optional<Resume> resumeOptional = resumeRepository.findById(resumeId);
        if (resumeOptional.isEmpty()) {
            return Optional.empty();
        }

        byte[] resumeData =  fileService.retrieveFile(resumeOptional.get().getId().toString(), resumeOptional.get().getFileName());
        return Optional.of(resumeData);
    }

    public void deleteResume(Integer resumeId) {
        Optional<Resume> resumeOptional = resumeRepository.findById(resumeId);
        if (resumeOptional.isEmpty()) {
            return;
        }

        fileService.deleteFile(resumeOptional.get().getId().toString(), resumeOptional.get().getFileName());
        resumeRepository.delete(resumeOptional.get());
    }
}
