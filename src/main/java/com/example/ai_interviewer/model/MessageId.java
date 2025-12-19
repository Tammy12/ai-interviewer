package com.example.ai_interviewer.model;

import lombok.Data;

@Data
// composite primary key
public class MessageId {
    private Integer messageId;
    private Integer interviewId;
}
