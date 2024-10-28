package com.app.lab2.tcp;

import lombok.Getter;

@Getter
enum TaskType {
    WRITE(1), READ(2);
    private final int priority;

    TaskType(int priority) {
        this.priority = priority;
    }

}
