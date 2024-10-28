package com.app.lab2.tcp;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Random;

import static com.app.lab2.tcp.FileAccess.ensureFileExists;

public class WebSocketHandler extends TextWebSocketHandler {
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final String FILE_PATH = "data.txt";
    private final PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>();

    public WebSocketHandler() {
        new Thread(this::processQueue).start();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        ensureFileExists();

        String payload = message.getPayload();
        System.out.println("Received: " + payload);

        if (payload.startsWith("write ") || payload.startsWith("writeline ")) {
            System.out.println("Adding write task to the queue");
            taskQueue.add(new Task(() -> processMessage(payload, session), TaskType.WRITE));
        } else if (payload.startsWith("read") || payload.startsWith("readline ")) {
            System.out.println("Adding read task to the queue");
            taskQueue.add(new Task(() -> processMessage(payload, session), TaskType.READ));
        } else if (payload.equalsIgnoreCase("delete")) {
            taskQueue.add(new Task(() -> processMessage(payload, session), TaskType.WRITE));
        } else {
            sendMessage(session, "Unknown command");
        }
    }

    private void processQueue() {
        while (true) {
            try {
                Task task = taskQueue.take();
                System.out.println("Processing " + task.getTaskType() + " task");
                task.run();
                Thread.sleep(50); // Optional delay to let tasks accumulate

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Queue processing interrupted");
                break;
            }
        }
    }

    private void processMessage(String message, WebSocketSession session) {
        if (message.startsWith("write ")) {
            String data = message.substring("write".length()).trim();
            if (data.isEmpty()) {
                sendMessage(session, "Error: No data provided for write command");
            } else {
                writeFile(data).thenRun(() -> sendMessage(session, "Write operation completed"));
            }
        } else if (message.startsWith("writeline ")) {
            String[] parts = message.split(" ", 3);
            int lineNumber = Integer.parseInt(parts[1]);
            String data = parts[2];
            writeToSpecificLine(lineNumber, data).thenRun(() -> sendMessage(session, "Data written to line " + lineNumber));
        } else if (message.startsWith("read")) {
            readFile().thenAccept(content -> sendMessage(session, "Read operation completed: " + content));
        } else if (message.equalsIgnoreCase("delete")) {
            deleteFile().thenRun(() -> sendMessage(session, "Delete operation completed"));
        } else if (message.startsWith("readline ")) {
            int lineNumber = Integer.parseInt(message.split(" ")[1]);
            readSpecificLine(lineNumber).thenAccept(line -> sendMessage(session, "Read line " + lineNumber + ": " + line));
        } else {
            sendMessage(session, "Unknown command");
        }
    }

    private CompletableFuture<Void> writeFile(String data) {
        return CompletableFuture.runAsync(() -> {
            lock.writeLock().lock();
            try {
                simulateRandomDelay();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
                    writer.write(data);
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.writeLock().unlock();
            }
        });
    }

    private CompletableFuture<Void> writeToSpecificLine(int lineNumber, String data) {
        return CompletableFuture.runAsync(() -> {
            lock.writeLock().lock();
            try {
                simulateRandomDelay();
                ensureFileExists();
                File tempFile = new File("temp.txt");
                File originalFile = new File(FILE_PATH);

                try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                    String line;
                    int currentLine = 0;
                    boolean lineWritten = false;

                    while ((line = reader.readLine()) != null) {
                        currentLine++;
                        if (currentLine == lineNumber) {
                            writer.write(data);
                            writer.newLine();
                            lineWritten = true;
                        } else {
                            writer.write(line);
                            writer.newLine();
                        }
                    }

                    if (!lineWritten) {
                        writer.write(data);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (originalFile.delete()) {
                    tempFile.renameTo(originalFile);
                } else {
                    System.err.println("Failed to delete the original file: " + originalFile.getAbsolutePath());
                }
            } finally {
                lock.writeLock().unlock();
            }
        });
    }

    private CompletableFuture<String> readFile() {
        return CompletableFuture.supplyAsync(() -> {
            lock.readLock().lock();
            StringBuilder content = new StringBuilder();
            try {
                simulateRandomDelay();
                try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.readLock().unlock();
            }
            return content.toString();
        });
    }

    private CompletableFuture<Void> deleteFile() {
        return CompletableFuture.runAsync(() -> {
            lock.writeLock().lock();
            try {
                simulateRandomDelay();
                File file = new File(FILE_PATH);
                if (file.exists()) {
                    file.delete();
                }
            } finally {
                lock.writeLock().unlock();
            }
        });
    }

    private CompletableFuture<String> readSpecificLine(int lineNumber) {
        return CompletableFuture.supplyAsync(() -> {
            lock.readLock().lock();
            try {
                simulateRandomDelay();
                ensureFileExists();
                try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
                    String line;
                    int currentLine = 0;
                    while ((line = reader.readLine()) != null) {
                        if (++currentLine == lineNumber) {
                            return line;
                        }
                    }
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                lock.readLock().unlock();
            }
        });
    }

    private void simulateRandomDelay() {
        try {
            int sleepTime = 1000 + new Random().nextInt(4000);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("Connection closed: " + status);
    }
}
