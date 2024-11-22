package com.app.raftleaderelection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RaftLeaderElectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(RaftLeaderElectionApplication.class, args);

        int basePort = 8090;
        int totalNodes = 3; // Adjust for the cluster size
        int[] allPorts = new int[totalNodes];

        for (int i = 0; i < totalNodes; i++) {
            allPorts[i] = basePort + i;
        }

        // Start each node with its ID and port, passing other node ports
        for (int i = 0; i < totalNodes; i++) {
            int[] otherPorts = new int[totalNodes - 1];
            int index = 0;
            for (int port : allPorts) {
                if (port != allPorts[i]) {
                    otherPorts[index++] = port;
                }
            }
            new Thread(() -> new Node(i + 1, allPorts[i], otherPorts)).start();
        }
    }

}
