package com.app.lab2.tcp;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
public class CommandWebSocketHandler extends TextWebSocketHandler {

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String command = message.getPayload();  // Get the command from the client

        // Process the command, e.g., interacting with the file or database
        String response = processCommand(command);

        // Send response back to the client
        session.sendMessage(new TextMessage(response));
    }

    private String processCommand(String command) {
        System.out.println("Received command: " + command);
        // You can implement different actions based on the command received
        if (command.startsWith("write ")) {
            String data = command.substring("write".length()).trim();
            FileAccess.writeToFile(data);
            return "Data written: " + data;
        } else if (command.startsWith("writeline")) {
            String[] parts = command.split(" ", 3);
            int lineNumber = Integer.parseInt(parts[1]);
            FileAccess.writeToSpecificLine(lineNumber, parts[2]);
            return "Data written to line " + lineNumber + ": " + parts[2];
        } else if (command.equalsIgnoreCase("read")) {
            return FileAccess.readFromFile();
        } else if (command.startsWith("delete ")) {
            int lineNumber = Integer.parseInt(command.split(" ")[1]);
            boolean success = FileAccess.deleteSpecificLine(lineNumber);
            return success ? "Line deleted" : "Error deleting line";
        } else if (command.equalsIgnoreCase("delete")) {
            return FileAccess.clearFile();
        } else if (command.startsWith("readline ")) {
            int lineNumber = Integer.parseInt(command.split(" ")[1]);
            String line = FileAccess.readSpecificLine(lineNumber);
            return (line != null) ? line : "Line not found";
        } else {
            return "Unknown command";
        }
    }
}

