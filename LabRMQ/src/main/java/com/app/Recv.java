package com.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Recv {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) {
        int numberOfReceivers = 3;

        for (int i = 0; i < numberOfReceivers; i++) {
            new Thread(new Receiver()).start();
        }
    }

    public static class Receiver implements Runnable {
        @Override
        public void run() {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.basicQos(1);
                System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(Thread.currentThread().getName() + " [x] Received '" + message + "'");
                };

                boolean manualAcknowledge = false;
                channel.basicConsume(QUEUE_NAME, manualAcknowledge, deliverCallback, consumerTag -> { });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
