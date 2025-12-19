package com.example.ai_interviewer.model;

public enum Stage {
    PROJECTS,
    TECH,
    TEAM,
    COMPANY,
    END;

    private static final Stage[] vals = values();

    public Stage next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }
}
