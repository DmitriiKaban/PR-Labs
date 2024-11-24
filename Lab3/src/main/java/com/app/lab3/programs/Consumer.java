package com.app.lab3.programs;

import com.app.lab3.config.RabbitMQConfig;
import com.app.lab3.utils.MultipartUploader;
import com.app.lab3.utils.UDPListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class Consumer {

    private final Channel channel;
    private final RabbitMQConfig config;
    private final UDPListener udpListener;
    private final MultipartUploader multipartUploader;
    private static final String SERVER_URL = "http://";
    private static final String SERVER_METHOD = "/uploadJson";

    @Scheduled(fixedRateString = "10", timeUnit = TimeUnit.SECONDS)
    public void consumeProducts() throws IOException {
        String queueName = config.getTaskQueueName();

        System.out.println(" [*] Waiting for messages from " + queueName);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");

            File jsonFile = new File("products.json");
            try (FileWriter writer = new FileWriter(jsonFile)) {
                writer.write(message);
            }

            if (udpListener.getLeaderPort() != null) {
                System.out.println("URL: " + SERVER_URL + udpListener.getLeaderAddress() + ":8081" +  SERVER_METHOD);
                multipartUploader.uploadFilePOST(SERVER_URL + udpListener.getLeaderAddress() + ":" + "8081" + SERVER_METHOD, jsonFile);
            }
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }
//
//    private void uploadProduct(Product product) {
//        String urlString = "http://" + udpListener.getLeaderAddress() + ":8081/createProduct";
////        System.out.println("URL: " + urlString);
//        try {
//            URL url = new URL(urlString);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setDoOutput(true);
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            String productJson = objectMapper.writeValueAsString(product);
//
//            try (OutputStream os = connection.getOutputStream()) {
//                byte[] input = productJson.getBytes("utf-8");
//                os.write(input, 0, input.length);
//            }
//
//            int responseCode = connection.getResponseCode();
//            System.out.println("Response Code: " + responseCode);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}


