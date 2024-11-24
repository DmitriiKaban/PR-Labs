package com.app.scraperlab3.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class RabbitMQConfig {

    private static final String TASK_QUEUE_NAME = "task_queue";

    @Bean
    public Connection rabbitMQConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        return factory.newConnection();
    }

    @Bean
    public Channel rabbitMQChannel(Connection connection) throws IOException {
        Channel channel = connection.createChannel();
        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        return channel;
    }

    public String getTaskQueueName() {
        return TASK_QUEUE_NAME;
    }
}
