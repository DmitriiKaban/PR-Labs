package com.app.scraperlab3.services;

import com.app.scraperlab3.config.RabbitMQConfig;
import com.app.scraperlab3.models.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class Publisher {

    private final Scraper scraper;
    private final Channel channel;
    private final RabbitMQConfig config;
    private final FTPUploader ftpUploader;

    @Scheduled(fixedRateString = "60", timeUnit = TimeUnit.SECONDS)
    public void publishProducts() throws IOException {
        List<Product> fetchedProducts = scraper.getProducts();
        System.out.println("Publishing products...");
        String message = new ObjectMapper().writeValueAsString(fetchedProducts);
        channel.basicPublish("", config.getTaskQueueName(), MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));

        System.out.println("Uploading products.json ...");
        File jsonFile = new File("products.json");
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(message);
        }

        ftpUploader.uploadFileToFTP(jsonFile);
    }
}
