package com.example.ai_interviewer.service;

import com.example.ai_interviewer.client.openai.OpenAIService;
import com.example.ai_interviewer.client.openai.exception.OpenAIFileUploadException;
import com.example.ai_interviewer.client.openai.exception.OpenAIResponsesException;
import com.example.ai_interviewer.client.openai.model.OpenAIFile;
import com.example.ai_interviewer.client.openai.model.Role;
import com.example.ai_interviewer.dto.InterviewDto;
import com.example.ai_interviewer.dto.MessageDto;
import com.example.ai_interviewer.dto.ResumeDto;
import com.example.ai_interviewer.exception.InterviewNotFoundException;
import com.example.ai_interviewer.exception.UnauthorizedException;
import com.example.ai_interviewer.model.Message;
import com.example.ai_interviewer.model.Stage;
import com.example.ai_interviewer.repository.MessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MessageService {
    private InterviewService interviewService;
    private ResumeService resumeService;
    private InstructionService instructionService;
    private S3FileService s3FileService;
    private OpenAIService openAIService;
    private MessageRepository messageRepository;

    // COMMENT: start the conversation when they submit the job description, that's when you should upload the file to openAI and create the messages
    public MessageDto processUserMessage(Integer resumeId, Integer interviewId, MessageDto messageDto) throws InterviewNotFoundException, UnauthorizedException, OpenAIFileUploadException, OpenAIResponsesException {
        InterviewDto interviewDto = interviewService.getInterview(resumeId, interviewId)
                .orElseThrow(() -> new InterviewNotFoundException());

        if (interviewDto.getStage().equals(Stage.PROJECTS)) {
            addResumeAndJobDescriptionMessages(resumeId, interviewDto);
        }

        addNewUserMessage(interviewDto, messageDto);

        List<Message> allMessages = getAllMessages(interviewId);
        Message assistantMessage = openAIService.sendInputs(allMessages);
        addNewAssistantMessage(assistantMessage, interviewDto, allMessages.size());

        interviewService.updateInterviewStage(resumeId, interviewId, interviewDto.getStage().next());
        return MessageDto.builder()
                .role(Role.assistant.name())
                .input(assistantMessage.getContent())
                .build();
    }

    private void addResumeAndJobDescriptionMessages(Integer resumeId, InterviewDto interviewDto) throws OpenAIFileUploadException, UnauthorizedException {
        ResumeDto resume = resumeService.getResume(resumeId)
                .orElseThrow();
        byte[] fileBytes = s3FileService.retrieveFile(resumeId.toString(), resume.getFileName());
        OpenAIFile openAIFile = openAIService.uploadFile(fileBytes);

        Message resumeMessage = Message.builder()
                .messageId(0)
                .interviewId(interviewDto.getId())
                .role(Role.user.name())
                .content(openAIFile.getId())
                .build();
        messageRepository.save(resumeMessage);

        Message jobDescriptionMessage = Message.builder()
                .messageId(1)
                .interviewId(interviewDto.getId())
                .role(Role.user.name())
                .content(interviewDto.getJobDescription())
                .build();
        messageRepository.save(jobDescriptionMessage);
    }

    private void addNewUserMessage(InterviewDto interviewDto, MessageDto messageDto) {
        String instruction = instructionService.getInstruction(interviewDto.getStage());
        List<Message> allMessages = getAllMessages(interviewDto.getId());

        Message userMessage = Message.builder()
                .messageId(allMessages.size())
                .interviewId(interviewDto.getId())
                .role(Role.user.name())
                .content(messageDto.getInput())
                .build();
        messageRepository.save(userMessage);

        Message instructionMessage = Message.builder()
                .messageId(allMessages.size() + 1)
                .interviewId(interviewDto.getId())
                .role(Role.developer.name())
                .content(instruction)
                .build();
        messageRepository.save(instructionMessage);
    }

    private void addNewAssistantMessage(Message message, InterviewDto interviewDto, Integer messagesCount) {
        message.setInterviewId(interviewDto.getId());
        message.setMessageId(messagesCount);
        messageRepository.save(message);
    }

    public List<Message> getAllMessages(Integer interviewId) {
        return messageRepository.findAllByInterviewIdOrderByMessageIdAsc(interviewId);
    }
}
