package com.app.lab2.tcp;

import java.io.*;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.*;

public class FileAccess {

    private static final String FILE_PATH = "data.txt";
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Semaphore writePrioritySemaphore = new Semaphore(1, true); // Ensures write-priority
    private static int pendingWrites = 0; // Counter for pending write operations
    private static final Object writeLock = new Object(); // Lock for write counter

    static void ensureFileExists() {
        try {
            if (!Files.exists(Paths.get(FILE_PATH))) {
                Files.createFile(Paths.get(FILE_PATH));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String data) {
        try {
            synchronized (writeLock) {
                pendingWrites++;
            }

            writePrioritySemaphore.acquire(); // Block read requests until all writes are done
            lock.writeLock().lock();

            ensureFileExists();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
                writer.write(data);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.writeLock().unlock();
                synchronized (writeLock) {
                    pendingWrites--;
                    if (pendingWrites == 0) {
                        writePrioritySemaphore.release(); // Allow reads when no pending writes
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static String readFromFile() {
        try {
            writePrioritySemaphore.acquire(); // Block until all writes are done
            lock.readLock().lock();

            ensureFileExists();
            return new String(Files.readAllBytes(Paths.get(FILE_PATH)));
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading file";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Read interrupted";
        } finally {
            lock.readLock().unlock();
            writePrioritySemaphore.release();
        }
    }

    public static String readSpecificLine(int lineNumber) {
        try {
            writePrioritySemaphore.acquire(); // Block until all writes are done
            lock.readLock().lock();

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
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error reading file";
        } finally {
            lock.readLock().unlock();
            writePrioritySemaphore.release();
        }
    }

    public static boolean deleteSpecificLine(int lineNumber) {

        try {
            synchronized (writeLock) {
                pendingWrites++;
            }

            writePrioritySemaphore.acquire(); // Block read requests until all writes are done
            lock.writeLock().lock();

            ensureFileExists();
            File tempFile = new File("temp.txt");
            File originalFile = new File(FILE_PATH);

            try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                int currentLine = 0;
                boolean lineDeleted = false;

                while ((line = reader.readLine()) != null) {
                    if (++currentLine == lineNumber) {
                        lineDeleted = true;
                        continue;
                    }
                    writer.write(line);
                    writer.newLine();
                }

                if (lineDeleted) {
                    Files.delete(Paths.get(FILE_PATH));
                    return tempFile.renameTo(originalFile);
                } else {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
            synchronized (writeLock) {
                pendingWrites--;
                if (pendingWrites == 0) {
                    writePrioritySemaphore.release(); // Allow reads when no pending writes
                }
            }
        }
    }

    public static boolean writeToSpecificLine(int lineNumber, String data) {

        try {
            synchronized (writeLock) {
                pendingWrites++;
            }

            writePrioritySemaphore.acquire(); // Block read requests until all writes are done
            lock.writeLock().lock();

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
            }

            if (originalFile.delete()) {
                return tempFile.renameTo(originalFile);
            } else {
                System.err.println("Failed to delete the original file: " + originalFile.getAbsolutePath());
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            {
                lock.writeLock().unlock();
                synchronized (writeLock) {
                    pendingWrites--;
                    if (pendingWrites == 0) {
                        writePrioritySemaphore.release(); // Allow reads when no pending writes
                    }
                }
            }
        }
    }

    public static String clearFile() {
        try {

            synchronized (writeLock) {
                pendingWrites++;
            }

            writePrioritySemaphore.acquire(); // Block read requests until all writes are done
            lock.writeLock().lock();

            ensureFileExists();
            Files.delete(Paths.get(FILE_PATH));
            Files.createFile(Paths.get(FILE_PATH));
            return "File cleared";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error clearing file";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
            synchronized (writeLock) {
                pendingWrites--;
                if (pendingWrites == 0) {
                    writePrioritySemaphore.release(); // Allow reads when no pending writes
                }
            }
        }
    }
}
