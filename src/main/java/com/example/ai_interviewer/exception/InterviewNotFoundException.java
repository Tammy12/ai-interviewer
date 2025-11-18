package com.example.ai_interviewer.exception;

public class InterviewNotFoundException extends Exception {
    public InterviewNotFoundException() {
        super("Interview not found.");
    }
}
