package com.example.ai_interviewer.service;

import com.example.ai_interviewer.exception.S3UploadException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class S3FileService {
    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.accessKey}")
    private String accessKey;

    @Value("${aws.s3.secretKey}")
    private String secretKey;

    private S3Client s3Client;

    @PostConstruct
    private void initialize() {
        // COMMENT: move S3Client to an S3Config class. Use method injection for the configuration values
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.builder()
                .accessKeyId(accessKey)
                .secretAccessKey(secretKey)
                .build();
        s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .region(Region.US_EAST_1)
                .build();
    }

    public void uploadFile(MultipartFile multipartFile, String folder) throws S3UploadException {
        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("content_type", multipartFile.getContentType());
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(folder + "/" + multipartFile.getOriginalFilename())
                    .metadata(metadata)
                    .build();

            RequestBody requestBody = RequestBody.fromBytes(IOUtils.toByteArray(multipartFile.getInputStream()));

           PutObjectResponse response = s3Client.putObject(objectRequest, requestBody);
        } catch (S3Exception | IOException e) {
            throw new S3UploadException(e);
        }
    }

    public byte[] retrieveFile(String folderName, String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(folderName + "/" + fileName)
                .build();
        // COMMENT: you should catch potential exceptions from S3 in all these cases
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(getObjectRequest);
        return response.asByteArray();
    }

    public void deleteFile(String folderName, String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(folderName + "/" + fileName)
                .build();
        // COMMENT: you should catch potential exceptions from S3 in all these cases
        s3Client.deleteObject(deleteObjectRequest);
    }
}
