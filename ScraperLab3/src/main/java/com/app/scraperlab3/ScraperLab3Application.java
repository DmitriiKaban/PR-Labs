package com.app.scraperlab3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ScraperLab3Application {

    public static void main(String[] args) {
        SpringApplication.run(ScraperLab3Application.class, args);
    }

}
