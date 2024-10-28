package com.app.lab2.tcp;

import java.util.concurrent.PriorityBlockingQueue;

public class QueueProcessor {
    private final PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>();

    // Method to add tasks to the priority queue
    public void addTask(Runnable task, TaskType taskType) {
        System.out.println("Received: " + taskType);
        taskQueue.add(new Task(task, taskType));
        System.out.println("Task queue size: " + taskQueue.size());
    }

    // Main queue processing loop
    public void processQueue() {
        while (true) {
            try {
                Task task = taskQueue.take(); // Processes tasks based on priority
                System.out.println("Processing " + task.getTaskType() + " task");
                task.run();
                System.out.println("Task queue size: " + taskQueue.size());

                Thread.sleep(50); // Optional: Simulate task processing time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Queue processing interrupted");
                break;
            }
        }
    }
}

