package com.app.lab2.raft;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node {

    private State currentState;
    private DatagramSocket socket;
    private static final int BASE_TIMEOUT = 1000;
    private static final int TIMEOUT_VARIANCE = 1500;
    private static final int HEARTBEAT_INTERVAL = 3000;
    private static int CLUSTER_SIZE;
    private final AtomicBoolean leaderAlive = new AtomicBoolean(false);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService electionExecutor = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService heartbeatTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> heartbeatTimeoutTask;
    private final Object timerLock = new Object();

    private final int nodeId;
    private final int port;
    private final int[] otherPorts;
    private List<Integer> discoveredPorts;
    Map<Integer, String> addressPortMap;

    private int currentTerm = 0;
    private int votesGranted = 0;

    public Node(int nodeId, int port, int[] otherPorts, Map<Integer, String> addressPortMap) {
        this.nodeId = nodeId;
        this.port = port;
        this.otherPorts = otherPorts;
        CLUSTER_SIZE = otherPorts.length + 1;
        discoveredPorts = new ArrayList<>();
        this.addressPortMap = addressPortMap;

        try {
            this.socket = new DatagramSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        startNode();
    }

    private void startNode() {
        try {

            sendMessage("HELP " + nodeId, 8093);

            startUDPListener();
            Thread.sleep(1000);
            discoverNodes();
            Thread.sleep(1000);
            System.out.println("I " + nodeId + ", from port " + port + " discovered ports: " + discoveredPorts);

            if (discoveredPorts.isEmpty()) {
                System.out.println("Node " + nodeId + " is the only node in the cluster.");
                currentState = State.LEADER;
                leaderAlive.set(true);
                sendHeartbeats();
            } else {
                startElection();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void startUDPListener() {
        new Thread(() -> {
            System.out.println("Node " + nodeId + " is listening on port " + port);
            try {
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
        }).start();
    }

    private void startElection() {

        synchronized (timerLock) {

            if (currentState == State.LEADER) {
                System.out.println("Node " + nodeId + ": Already a leader, skipping election.");
                return;
            }
            if (leaderAlive.get()) {
                System.out.println("Node " + nodeId + ": Election canceled as leader is active.");
                return;
            }

            currentState = State.CANDIDATE;
            currentTerm++;
            votesGranted = 0;
            leaderAlive.set(false);

            System.out.println("Node " + nodeId + " starting election for term " + currentTerm + "...");

            int randomPort = otherPorts[new Random().nextInt(otherPorts.length)];
            sendMessage("VOTE for " + (randomPort - 8089) + " term " + currentTerm + " from " + nodeId + " with port " + port, randomPort);
            startOrRestartHeartbeatTimeout();
            electionExecutor.schedule(() -> {
                if (votesGranted > CLUSTER_SIZE / 2) {
                    becomeLeader();
//                System.out.println("Node " + nodeId + " received  " + votesGranted + " votes during term " + currentTerm);
                } else {
                    startOrRestartHeartbeatTimeout();
                    currentState = State.FOLLOWER;
//                System.out.println("Node " + nodeId + " received  " + votesGranted + " votes during term " + currentTerm);
                }
            }, BASE_TIMEOUT, TimeUnit.MILLISECONDS);
        }
    }

    private void becomeLeader() {
        currentState = State.LEADER;
        leaderAlive.set(true);
        heartbeatTimeoutExecutor.shutdownNow();
        System.out.println("Node " + nodeId + " is now the leader for term " + currentTerm + "!");

        for (int otherPort : otherPorts) {
            sendMessage("LEADER_ANNOUNCE " + nodeId + " " + currentTerm, otherPort);
        }

        sendMessage("LEADER " + port, 9090);

        sendHeartbeats();
    }

    private void sendHeartbeats() {
        final int[] sentHeartbeats = {0};
        heartbeatExecutor.scheduleAtFixedRate(() -> {

            if (sentHeartbeats[0] >= 3) {
                System.out.println("Node " + nodeId + " is no longer the leader for term " + currentTerm + "!");
                leaderAlive.set(false);
                heartbeatExecutor.shutdown();
                heartbeatExecutor = new ScheduledThreadPoolExecutor(1);
                return;
            }

            startOrRestartHeartbeatTimeout();

            sentHeartbeats[0]++;
            for (int otherPort : otherPorts) {
                if (otherPort == port) continue;
                sendMessage("HEARTBEAT term: " + currentTerm + " nodeId: " + nodeId, otherPort);
            }

        }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void handleMessage(String message, InetAddress address, int senderPort) {
        String[] parts = message.split(" ");
        String type = parts[0];

        if (!message.startsWith("PING") && !message.startsWith("PONG")) {
            System.out.println("Node " + nodeId + " received message: " + message);
        }

        switch (type) {
            case "HEARTBEAT":
                int leaderTerm = Integer.parseInt(parts[2]);
                int leaderId = Integer.parseInt(parts[4]);
                if (leaderTerm >= currentTerm) {
                    leaderAlive.set(true);
                    currentState = State.FOLLOWER;
                    currentTerm = leaderTerm;
                    startOrRestartHeartbeatTimeout();
//                    resetHeartbeatTimeout();
//                    System.out.println("Node " + nodeId + ": Recognized leader " + leaderId + " for term " + leaderTerm);
                }
                break;

            case "VOTE":
                int term = Integer.parseInt(parts[4]);
//                int voterId = Integer.parseInt(parts[6]);
//                int voterPort = Integer.parseInt(parts[9]);

                if (term == currentTerm && currentState == State.CANDIDATE) {
                    votesGranted++;
                    if (votesGranted >= CLUSTER_SIZE / 2) {
                        becomeLeader();
                    }
//                    System.out.println("Node " + nodeId + " received a vote from Node " + voterId + ", port: " + voterPort);
                }
                break;

            case "LEADER_ANNOUNCE":
                int newLeaderId = Integer.parseInt(parts[1]);
                int newLeaderTerm = Integer.parseInt(parts[2]);
                if (newLeaderTerm >= currentTerm) {
                    leaderAlive.set(true);
                    currentState = State.FOLLOWER;
                    currentTerm = newLeaderTerm;
                    resetElectionTimer();
                    System.out.println("Node " + nodeId + ": Recognized new leader " + newLeaderId + " for term " + newLeaderTerm);
                }
                break;

            case "PING":
                sendMessage("PONG " + nodeId, senderPort);
                break;

            case "HELP":
                sendMessage("HELP-BACK " + nodeId, senderPort);
                break;

            case "PONG":
                discoveredPorts.add(senderPort);
                break;

            default:
                System.out.println("Unknown message type: " + message);
        }
    }

    private void sendMessage(String message, int targetPort) {
        executor.submit(() -> {
            try {
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(addressPortMap.get(targetPort)), targetPort);
                System.out.println("Sending message to address: " + addressPortMap.get(targetPort) + " port: " + targetPort);
                socket.send(packet);
                if (message.startsWith("PING") || message.startsWith("PONG") || message.startsWith("VOTE") || message.startsWith("LEADER_ANNOUNCE") || message.startsWith("HEARTBEAT"))
                    return;

                System.out.println("Node " + nodeId + " sent: " + message + " to port " + targetPort);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void resetElectionTimer() {
        executor.schedule(() -> {
            if (!leaderAlive.get() && currentState != State.LEADER) {
                startElection();
            }
        }, BASE_TIMEOUT + new Random().nextInt(TIMEOUT_VARIANCE), TimeUnit.MILLISECONDS);
    }

    public void discoverNodes() {
        executor.submit(() -> {
            System.out.println("Node " + nodeId + " on port " + port + ": Starting sending PING to all other nodes...");
            for (int targetPort : otherPorts) {
                sendMessage("PING " + nodeId, targetPort); // Send PING to all other nodes
            }
        });
    }

    private void startOrRestartHeartbeatTimeout() {
        synchronized (timerLock) {
            if (heartbeatTimeoutTask != null && !heartbeatTimeoutTask.isDone()) {
                heartbeatTimeoutTask.cancel(false);
            }

            heartbeatTimeoutTask = scheduler.schedule(() -> {
                System.out.println("Node " + nodeId + ": Heartbeat timeout! Starting election...");
                leaderAlive.set(false);

//                CLUSTER_SIZE--;
                startElection();
            }, HEARTBEAT_INTERVAL * 2, TimeUnit.MILLISECONDS);
        }
    }

}
