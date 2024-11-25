package com.app.rpc;

import com.rabbitmq.client.*;
import org.json.JSONObject;

public class RPCReceiver {

    private static final String RPC_QUEUE_NAME = "PAYMENT";
    private static final String USERNAME = "faf2xx";
    private static final String PASSWORD = "faf2xx";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("207.154.252.103");
        factory.setUsername(USERNAME); // Set credentials before creating the connection
        factory.setPassword(PASSWORD);

        // Create the connection and channel
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        channel.queuePurge(RPC_QUEUE_NAME);

        channel.basicQos(1);

        System.out.println(" [x] Awaiting RPC requests");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String response = "";
            try {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(message);

                response = validatePayment(message);
            } catch (RuntimeException e) {
                System.out.println(" [.] " + e);
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, consumerTag -> {});
    }

    private static String validatePayment(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String cardNumber = json.getString("c_number");
            String cvv = json.getString("cvv");
            String date = json.getString("date");
            if (cardNumber.length() != 16) {
                return "Invalid card number.";
            }
            if (cvv.length() != 3) {
                return "Invalid CVV.";
            }
            if (!date.matches("\\d{4}-\\d{2}")) {
                return "Invalid date format.";
            }
            return "Payment details are valid.";
        } catch (Exception e) {
            return "Invalid JSON message.";
        }
    }
}
