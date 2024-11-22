package com.app.raftleaderelection;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node {

    private State currentState;
    private static final int BASE_TIMEOUT = 1500; // Base timeout in ms
    private static final int TIMEOUT_VARIANCE = 1500; // Random variance in ms
    private static final int HEARTBEAT_INTERVAL = 1000; // Heartbeat interval in ms
    private static final int CLUSTER_SIZE = 3; // Number of nodes in the cluster
    private final AtomicBoolean leaderAlive = new AtomicBoolean(false);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1); // Use ScheduledExecutorService
    private final int nodeId;
    private final int port;
    private final int[] otherPorts;

    private int currentTerm = 0; // Term counter for elections
    private int votesGranted = 0; // Tracks votes received

    public Node(int nodeId, int port, int[] otherPorts) {
        this.nodeId = nodeId;
        this.port = port;
        this.otherPorts = otherPorts;

        startNode();
    }

    private void startNode() {
        discoverNodes();
        startUDPListener();
        startElectionTimer();
    }

    private void startUDPListener() {
        executor.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    handleMessage(message, packet.getAddress(), packet.getPort());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startElectionTimer() {
        executor.scheduleAtFixedRate(() -> {
            try {
                int randomizedTimeout = BASE_TIMEOUT + new Random().nextInt(TIMEOUT_VARIANCE);
                Thread.sleep(randomizedTimeout);

                if (!leaderAlive.get() && currentState != State.LEADER) {
                    startElection();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, 1, TimeUnit.SECONDS); // Periodic task, check for elections every second
    }

    private void startElection() {
        currentState = State.CANDIDATE;
        currentTerm++;
        votesGranted = 1; // Vote for self
        leaderAlive.set(false);

        System.out.println("Node " + nodeId + " starting election for term " + currentTerm + "...");

        int randomPort = otherPorts[new Random().nextInt(otherPorts.length)];
        sendMessage("VOTE " + currentTerm + " " + nodeId, randomPort);

        // Wait for votes (simulated with delay)
        executor.schedule(() -> {
            if (votesGranted > CLUSTER_SIZE / 2) {
                becomeLeader();
            } else {
                currentState = State.FOLLOWER;
            }
        }, BASE_TIMEOUT / 2, TimeUnit.MILLISECONDS); // Election result after timeout
    }

    private void becomeLeader() {
        currentState = State.LEADER;
        leaderAlive.set(true);
        System.out.println("Node " + nodeId + " is now the leader for term " + currentTerm + "!");

        for (int otherPort : otherPorts) {
            sendMessage("LEADER_ANNOUNCE " + nodeId, otherPort);
        }

        sendHeartbeats();
    }

    private void sendHeartbeats() {
        executor.submit(() -> {
            while (leaderAlive.get()) {
                for (int otherPort : otherPorts) {
                    sendMessage("HEARTBEAT " + currentTerm + " " + nodeId, otherPort);
                }
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void handleMessage(String message, InetAddress address, int senderPort) {
        String[] parts = message.split(" ");
        String type = parts[0];

        switch (type) {
            case "HEARTBEAT":
                int leaderTerm = Integer.parseInt(parts[1]);
                if (leaderTerm >= currentTerm) {
                    leaderAlive.set(true);
                    currentState = State.FOLLOWER;
                }
                break;

            case "VOTE":
                int term = Integer.parseInt(parts[1]);
                int voterId = Integer.parseInt(parts[2]);

                // Accept the vote if in the same term and not yet a leader
                if (term == currentTerm && currentState == State.CANDIDATE) {
                    votesGranted++;
                    System.out.println("Node " + nodeId + " received a vote from Node " + voterId);
                }
                break;

            case "LEADER_ANNOUNCE":
                int leaderId = Integer.parseInt(parts[1]);
                leaderAlive.set(true);
                currentState = State.FOLLOWER;
                System.out.println("Node " + leaderId + " is the new leader for term " + currentTerm);
                break;

            case "PING":
                sendMessage("PONG " + nodeId, senderPort);
                break;

            default:
                System.out.println("Unknown message type: " + message);
        }
    }

    private void sendMessage(String message, int targetPort) {
        executor.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), targetPort);
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void discoverNodes() {
        executor.submit(() -> {
            try {
                System.out.println("Starting node discovery...");

                // PING
                for (int targetPort = 8090; targetPort <= 8096; targetPort++) {
                    sendMessage("PING " + nodeId, targetPort);
                }

                // PONG
                DatagramSocket socket = new DatagramSocket(port);
                socket.setSoTimeout(2000);

                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("Received: " + message + " from " + packet.getPort());
                    } catch (SocketTimeoutException e) {
                        // Timeout reached, stop listening
                        break;
                    }
                }
                socket.close();

                System.out.println("Node discovery complete.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
