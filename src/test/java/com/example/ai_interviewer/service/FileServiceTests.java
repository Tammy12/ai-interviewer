package com.example.ai_interviewer.service;

import com.example.ai_interviewer.exception.S3UploadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FileServiceTests {
    private String bucketName;
    @Mock
    private S3Client s3Client;
    @InjectMocks
    private FileService fileService;

    public FileServiceTests(@Value("${aws.s3.bucketName}") String bucketName) {
        this.bucketName = bucketName;
    }

    @Test
    public void testUploadFile() throws S3UploadException {
        // arrange
        String fileName = "mylittlefile.pdf";
        String folderName = "3";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("content_type", "application/pdf");

        byte[] fileData = "data here".getBytes();
        MockMultipartFile file = new MockMultipartFile(fileName, fileName, "application/pdf", fileData);

        PutObjectRequest expectedObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(folderName + "/" + fileName)
                .metadata(metadata)
                .build();
        RequestBody expectedRequestBody = RequestBody.fromBytes(fileData);
        // act
        fileService.uploadFile(file, folderName);

        // assert
        verify(s3Client).putObject(expectedObjectRequest, expectedRequestBody);

    }
}
