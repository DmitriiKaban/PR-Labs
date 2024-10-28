package com.app.lab2.tcp;

import lombok.Getter;

@Getter
class Task implements Comparable<Task> {
    private final Runnable task;
    private final TaskType taskType;

    public Task(Runnable task, TaskType taskType) {
        this.task = task;
        this.taskType = taskType;
    }

    public void run() {
        task.run();
    }

    @Override
    public int compareTo(Task other) {
        return Integer.compare(this.taskType.getPriority(), other.taskType.getPriority());
    }
}


