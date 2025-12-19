package com.example.ai_interviewer.service;

import com.example.ai_interviewer.model.Stage;
import org.springframework.stereotype.Service;

@Service
public class InstructionService {
    // COMMENT: turn into util and use switch statement. Annotate with @UtilityClass
    public String getInstruction(Stage stage) {
        if (stage.equals(Stage.PROJECTS)) {
            return "Using a single non-compound sentence, ask a job interview question about work projects the user has worked on in the past.";
        } else if (stage.equals(Stage.TECH)) {
            return "Respond with a single non-compound sentence. Ask a job interview question about an important technical skill from the job description.";
        } else if (stage.equals(Stage.TEAM)) {
            return "Using a single non-compound sentence, ask a job interview question about interpersonal skills referenced in the job description.";
        } else if (stage.equals(Stage.COMPANY)) {
            return "Respond with a single non-compound sentence. Ask a job interview question about why the user wants to work at the company from the job description.";
        } else if (stage.equals(Stage.END)) {
            return "Thank the user for the interview and say goodbye.";
        } else return null;
    }
}
