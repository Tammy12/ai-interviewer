package com.example.ai_interviewer.exception;

public class ResumeNotFoundException extends Exception {
    public ResumeNotFoundException() {
        super("Resume not found.");
    }
}
