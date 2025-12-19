package com.example.ai_interviewer.client.openai;

import com.example.ai_interviewer.client.openai.exception.OpenAIFileUploadException;
import com.example.ai_interviewer.client.openai.exception.OpenAIResponsesException;
import com.example.ai_interviewer.client.openai.model.*;
import com.example.ai_interviewer.exception.UnauthorizedException;
import com.example.ai_interviewer.model.Message;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
// COMMENT: should be called OpenAIClient
public class OpenAIService {
    private RestTemplate restTemplate;

    public OpenAIFile uploadFile(byte[] file) throws UnauthorizedException, OpenAIFileUploadException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(file) {
            @Override
            public String getFilename() {
                return "testing.pdf";
            }
        };
        body.add("file", resource);
        body.add("purpose", "user_data");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<OpenAIFile> response = null;
        try {
            response = restTemplate.postForEntity("/files", requestEntity, OpenAIFile.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new UnauthorizedException("Unauthorized access to OpenAI.");
            }
        }

        if (response == null || response.getStatusCode() != HttpStatus.OK) {
            throw new OpenAIFileUploadException();
        } else {
            return response.getBody();
        }
    }

    public Message sendInputs(List<Message> messages) throws OpenAIResponsesException {
        List<Input> inputs = getAllInputs(messages);

        OpenAIRequest request = OpenAIRequest.builder()
                .model("gpt-5-nano")
                .input(inputs)
                .build();

        ResponseEntity<OpenAIResponse> response = restTemplate.postForEntity("/responses", request, OpenAIResponse.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new OpenAIResponsesException("Unsuccessful call to OpenAI: " + response.getStatusCode());
        }

        Optional<List<Content>> contentList = response.getBody().getOutput().stream()
                .filter(output -> output.getType().equals("message"))
                .findFirst()
                .map(output -> output.getContent());

        if (contentList.isEmpty()) {
            throw new OpenAIResponsesException("Unsuccessful call to OpenAI: No output text.");
        }

        Optional<String> responseText = contentList.get().stream()
                .filter(content -> content.getType().equals("output_text"))
                .findFirst()
                .map(content -> content.getText());

        if (responseText.isEmpty()) {
            throw new OpenAIResponsesException("Unsuccessful call to OpenAI: No output text.");
        }

        return Message.builder()
                .role(Role.assistant.name())
                .content(responseText.get())
                .build();
    }

    private List<Input> getAllInputs(List<Message> messages) {
        // COMMENT: add a contentType field to the message and then make the file handling a conditional thing inside the for loop. Change for loop to for each
        List<Input> allInputs = new ArrayList<>();
        InputFile inputFile = InputFile.builder()
                .file_id(messages.get(0).getContent())
                .build();
        Input resumeInput = Input.builder()
                .role(Role.user.name())
                .content(List.of(inputFile))
                .build();
        allInputs.add(resumeInput);

        for (int i = 1; i < messages.size(); i++) {
            Input input = Input.builder()
                    .role(messages.get(i).getRole())
                    .content(messages.get(i).getContent())
                    .build();
            allInputs.add(input);
        }

        return allInputs;
    }
}
