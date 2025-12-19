package com.example.ai_interviewer.exception;

public class S3UploadException extends Exception {
    public S3UploadException(Throwable cause ) {
        super("Error uploading to S3", cause);
    }
}
