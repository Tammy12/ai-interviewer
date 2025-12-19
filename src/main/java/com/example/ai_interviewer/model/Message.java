package com.example.ai_interviewer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MessageId.class)
@Builder(toBuilder = true)
@Table(name = "MESSAGE")
public class Message {
    @Id
    @Column(name = "message_id")
    private Integer messageId;
    @Id
    @Column(name = "interview_id")
    private Integer interviewId;
    @Column(name = "role")
    // COMMENT: this should be the enum, not a string
    private String role;
    @Column(name = "content")
    private String content;
}
