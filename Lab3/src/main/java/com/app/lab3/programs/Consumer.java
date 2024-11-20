package com.app.lab3.programs;

import com.app.lab3.config.RabbitMQConfig;
import com.app.lab3.utils.FTPUploader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class Consumer {

    private final Channel channel;
    private final RabbitMQConfig config;
    private final FTPUploader ftpUploader;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String SERVER_URL = "http://localhost:8081/uploadJson";

    private String server = "localhost";
    private int port = 21;
    private String user = "testuser";
    private String pass = "testpass";
    private String localFilePath = "products.json";
    private String remoteFilePath = "/products.json";

    @Scheduled(fixedRateString = "1", timeUnit = TimeUnit.SECONDS)
    public void consumeProducts() throws IOException {
        String queueName = config.getTaskQueueName();

        System.out.println(" [*] Waiting for messages from " + queueName);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");

            try {
                // Step 1: Save the message as a .json file
                File jsonFile = new File("products.json");
                try (FileWriter writer = new FileWriter(jsonFile)) {
                    writer.write(message);
                }

                // Step 2: Send the file via POST request

                ftpUploader.uploadFileToFTP(jsonFile);

            } catch (Exception e) {
                e.printStackTrace();
            }

        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }

    public void sendFileToServer(File file) {
        RestTemplate restTemplate = new RestTemplate();

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Prepare the file as a resource
        FileSystemResource fileResource = new FileSystemResource(file);

        // Create the multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Send the POST request
        ResponseEntity<String> response = restTemplate.postForEntity(SERVER_URL, requestEntity, String.class);

        System.out.println("Response from server: " + response.getBody());
    }
}


