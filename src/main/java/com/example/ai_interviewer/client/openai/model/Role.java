package com.example.ai_interviewer.client.openai.model;

public enum Role {
    // COMMENT: needs to be all caps, you should add a field to this and annotate the getter with @JsonValue so jackson knows to use that as the value when turning it into JSON
    user,
    assistant,
    developer;
}
