package com.app.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class PaymentQueueService implements AutoCloseable {

    private static final String QUEUE_NAME = "PAYMENT";
    private static final String HOST = "207.154.252.103";
    private static final String USERNAME = "faf2xx";
    private static final String PASSWORD = "faf2xx";

    private Connection connection;
    private Channel channel;

    public PaymentQueueService() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        connection = factory.newConnection();
        channel = connection.createChannel();
    }
 
    public static void main(String[] args) {
        try (PaymentQueueService paymentRpc = new PaymentQueueService()) {
            String paymentRequest = "{\"c_number\": 1234567891234567, \"cvv\": \"111\", \"date\": \"2028-10\"}";

            System.out.println(" [x] Sending payment request: " + paymentRequest);
            String response = paymentRpc.call(paymentRequest);
            System.out.println(" [.] Received payment response: " + response);
        } catch (IOException | TimeoutException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public String call(String message) throws IOException, InterruptedException, ExecutionException {
        final String corrId = UUID.randomUUID().toString();

        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", QUEUE_NAME, props, message.getBytes("UTF-8"));

        final CompletableFuture<String> response = new CompletableFuture<>();

        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.complete(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });


        String result = response.get();
        channel.basicCancel(ctag);
        return result;
    }

    @Override
    public void close() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }
}
