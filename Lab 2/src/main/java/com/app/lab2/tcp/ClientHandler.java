package com.app.lab2.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String command;

            while ((command = in.readLine()) != null) {
                if ("READ".equalsIgnoreCase(command)) {
                    String result = FileAccess.readFromFile();
                    out.println(result);
                } else if (command.startsWith("WRITE ")) {
                    String data = command.substring(6);  // Assuming command is "WRITE data"
                    FileAccess.writeToFile(data);
                    out.println("Write successful");
                } else if (command.startsWith("READ ")) {
                    int lineNumber = Integer.parseInt(command.substring(5).trim());
                    String result = FileAccess.readSpecificLine(lineNumber);
                    out.println(result != null ? result : "Error: Line not found");
                } else if (command.startsWith("DELETE ")) {
                    int lineNumber = Integer.parseInt(command.substring(7).trim());
                    boolean success = FileAccess.deleteSpecificLine(lineNumber);
                    out.println(success ? "Delete successful" : "Error: Line not found");
                } else if (command.startsWith("WRITE ")) {
                    String[] parts = command.split(" ", 3);
                    int lineNumber = Integer.parseInt(parts[1]);
                    String data = parts[2];
                    boolean success = FileAccess.writeToSpecificLine(lineNumber, data);
                    out.println(success ? "Write successful" : "Error writing to line");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
