package com.app.lab2.utils;

import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class Requests {

    private static final String host = "999.md";
    private static final String path = "/ru/list/computers-and-office-equipment/laptops";

    public String fetchHttpsResponse(int pageNumber) throws IOException {
        StringBuilder response = new StringBuilder();
        String pagePath = path + "?page=" + pageNumber;

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, 443)) {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            writer.print("GET " + pagePath + " HTTP/1.1\r\n");
            writer.print("Host: " + host + "\r\n");
            writer.print("Connection: close\r\n");
            writer.print("\r\n");
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String line;
            boolean isBody = false;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    isBody = true;
                    continue;
                }
                if (isBody) {
                    response.append(line).append("\n");
                }
            }
        }

        return response.toString();
    }
}
