package com.app.lab2.tcp;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileAccess {

    private static final String FILE_PATH = "data.txt";
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private static void ensureFileExists() {
        try {
            if (!Files.exists(Paths.get(FILE_PATH))) {
                Files.createFile(Paths.get(FILE_PATH));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String data) {
        lock.writeLock().lock();
        try {
            ensureFileExists();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
                writer.write(data);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static String readFromFile() {
        lock.readLock().lock();
        try {
            ensureFileExists();
            return new String(Files.readAllBytes(Paths.get(FILE_PATH)));
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading file";
        } finally {
            lock.readLock().unlock();
        }
    }

    public static String readSpecificLine(int lineNumber) {
        lock.readLock().lock();
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading file";
        } finally {
            lock.readLock().unlock();
        }
    }

    public static boolean deleteSpecificLine(int lineNumber) {
        lock.writeLock().lock();
        try {
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
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean writeToSpecificLine(int lineNumber, String data) {
        lock.writeLock().lock();
        try {
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
        } finally {

            // sleep 15 seconds to simulate a long operation
//            try {
//                Thread.sleep(15000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            lock.writeLock().unlock();
        }
    }

    public static String clearFile() {
        lock.writeLock().lock();
        try {
            ensureFileExists();
            Files.delete(Paths.get(FILE_PATH));
            Files.createFile(Paths.get(FILE_PATH));
            return "File cleared";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error clearing file";
        } finally {
            lock.writeLock().unlock();
        }
    }
}
