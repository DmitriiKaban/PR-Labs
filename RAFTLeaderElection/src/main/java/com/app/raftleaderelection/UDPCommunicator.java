package com.app.raftleaderelection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPCommunicator {
    private final int port;
    private final DatagramSocket socket;

    public UDPCommunicator(int port) throws SocketException {
        this.port = port;
        this.socket = new DatagramSocket(port);
    }

    public void sendMessage(String message, InetAddress address, int port) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }

    public void listenForMessages(MessageHandler handler) {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    handler.handle(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface MessageHandler {
        void handle(String message);
    }
}

