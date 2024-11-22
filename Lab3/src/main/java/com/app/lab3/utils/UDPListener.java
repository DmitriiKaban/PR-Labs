package com.app.lab3.utils;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Component
public class UDPListener {

    @Getter
    private String leaderPort;
    @Getter
    private String leaderAddress;
    private DatagramSocket socket;
    private boolean wasInvoked = false;

    public UDPListener() { }

    public void startUDPListener() {

        if (wasInvoked) {
            return;
        }

        wasInvoked = true;

        new Thread(() -> {
            try {
                if (socket == null) {
                    socket = new DatagramSocket(9090);
                }

                System.out.println("UDP listener started on port 9090");

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
            leaderAddress = parts[3];
        } else {
            System.out.println("Unknown message type: " + message);
        }
    }
}
