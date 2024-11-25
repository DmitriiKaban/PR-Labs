package com.app.lab3.programs;

import com.app.lab3.config.RabbitMQConfig;
import com.app.lab3.models.EmailDetails;
import com.app.lab3.services.SMTPService;
import com.app.lab3.utils.MultipartUploader;
import com.app.lab3.utils.UDPListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import lombok.RequiredArgsConstructor;
import org.apache.commons.net.smtp.SMTP;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class Consumer {

    private boolean sentEmail = false;
    private final Channel channel;
    private final SMTPService smtpService;
    private final RabbitMQConfig config;
    private final UDPListener udpListener;
    private final MultipartUploader multipartUploader;
    private static final String SERVER_URL = "http://";
    private static final String SERVER_METHOD = "/uploadJson";

    @Scheduled(fixedRateString = "10", timeUnit = TimeUnit.SECONDS)
    public void consumeProducts() throws IOException {

        if (!sentEmail) {
            System.out.println("Email sent to the admin");

            sentEmail = true;
            smtpService.sendSimpleMail(new EmailDetails("dmitrii.cravcenco@isa.utm.md", "Started Service lab3", "Server is up!"));
//            smtpService.sendMessage("dmitrii.cravcenco@isa.utm.md", "Started Service lab3", "Server is up!");
        }

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
}


