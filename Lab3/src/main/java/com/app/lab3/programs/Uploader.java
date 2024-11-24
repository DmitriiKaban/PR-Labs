//package com.app.lab3.programs;
//
//import com.app.lab3.config.RabbitMQConfig;
//import com.app.lab3.models.Product;
//import com.app.lab3.services.Scraper;
//import com.app.lab3.utils.FTPUploader;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.MessageProperties;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//@Component
//@RequiredArgsConstructor
//public class Uploader {
//
//    private final Scraper scraper;
//    private final FTPUploader ftpUploader;
//
//    @Scheduled(fixedRateString = "15", timeUnit = TimeUnit.SECONDS)
//    public void publishProducts() throws IOException {
//        List<Product> fetchedProducts = scraper.getProducts();
//        System.out.println("Uploading products.json ...");
//
//        String message = new ObjectMapper().writeValueAsString(fetchedProducts);
//        File jsonFile = new File("products.json");
//        try (FileWriter writer = new FileWriter(jsonFile)) {
//            writer.write(message);
//        }
//
//        ftpUploader.uploadFileToFTP(jsonFile);
//    }
//}
