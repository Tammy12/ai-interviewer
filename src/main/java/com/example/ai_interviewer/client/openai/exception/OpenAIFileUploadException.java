package com.example.ai_interviewer.client.openai.exception;

public class OpenAIFileUploadException extends Exception {
  public OpenAIFileUploadException() {
    super("Error uploading file to OpenAI.");
  }
}
