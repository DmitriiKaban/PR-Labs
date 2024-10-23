package com.app.lab2.tcp;

import org.springframework.boot.CommandLineRunner;

import java.io.*;
import java.net.Socket;

public class TcpClient implements CommandLineRunner {

    @Override
    public void run(String... args) {

        String serverAddress = "127.0.0.1";
        int port = 12345;

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            String input;

            while ((input = console.readLine()) != null) {
                out.println(input);
                String response = in.readLine();
                System.out.println("Server response: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

