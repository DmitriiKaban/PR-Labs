package com.app.lab2;

import com.app.lab2.raft.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@RequiredArgsConstructor
@EnableAsync
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
        p.setUrl(datasourceUrl);
        p.setDriverClassName("com.mysql.cj.jdbc.Driver");
        p.setUsername(datasourceUsername);
        p.setPassword(datasourcePassword);
        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);
        return datasource;
    }
}
