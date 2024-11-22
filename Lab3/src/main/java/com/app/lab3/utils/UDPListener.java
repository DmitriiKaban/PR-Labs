package com.app.lab3.utils;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Component
public class UDPListener {

    @Getter
    private String leaderPort;
    private DatagramSocket socket;

    public UDPListener() {
        try {
            socket = new DatagramSocket(9090);
            startUDPListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startUDPListener() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    handleMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleMessage(String message) {
        String[] parts = message.split(" ");
        String type = parts[0];

        if (type.equals("LEADER")) {
            leaderPort = parts[1];

            System.out.println("Leader port: " + leaderPort);
        } else {
            System.out.println("Unknown message type: " + message);
        }
    }
}
