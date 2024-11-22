package com.app;

public class App {
    public static void main(String[] args) {
        int basePort = 8090;
        int totalNodes = 4;
        int[] allPorts = new int[totalNodes];

        for (int i = 0; i < totalNodes; i++) {
            allPorts[i] = basePort + i;
        }

        for (int i = 0; i < totalNodes; i++) {
            int[] otherPorts = new int[totalNodes - 1];
            int index = 0;
            for (int port : allPorts) {
                if (port != allPorts[i]) {
                    otherPorts[index++] = port;
                }
            }
            int finalI = i;
            new Thread(() -> new Node(finalI + 1, allPorts[finalI], otherPorts)).start();
        }
    }
}
