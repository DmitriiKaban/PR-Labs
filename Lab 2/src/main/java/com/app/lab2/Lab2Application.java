package com.app.lab2;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.DataSource;

@SpringBootApplication
@RequiredArgsConstructor
public class Lab2Application {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    public static void main(String[] args) {
        SpringApplication.run(Lab2Application.class, args);
    }

    @Bean
    public DataSource dataSource() {

        PoolProperties p = new PoolProperties();
        // Add useUnicode=true and characterEncoding=utf8mb4 to the URL
        p.setUrl("jdbc:mysql://localhost:3308/laptops?useUnicode=true&characterEncoding=utf8");
        p.setDriverClassName("com.mysql.cj.jdbc.Driver");
        p.setUsername(datasourceUsername);
        p.setPassword(datasourcePassword);
        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);
        return datasource;
    }

}
