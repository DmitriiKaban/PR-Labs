package com.app.lab2.configs;

import com.app.lab2.raft.Node;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class NodeStarterConfig {

    @Autowired
    private ServletWebServerApplicationContext webServerAppContext;

    @PostConstruct
    public void NodeStarter() {

        String nodeId = System.getenv("NODE_ID");

        int portOfTheApp = webServerAppContext.getWebServer().getPort() + 11 + Integer.parseInt(nodeId);
        System.out.println("Port of the app: " + portOfTheApp);

        String[] ports = System.getenv("CLUSTER_PORTS").split(",");
        System.out.println("Ports of the cluster: " + Arrays.toString(ports));
        int[] otherPorts = Arrays.stream(ports).mapToInt(Integer::parseInt).toArray();

        String[] addresses = System.getenv("CLUSTER_ADDRESSES").split(",");
        System.out.println("Addresses of the cluster: " + Arrays.toString(addresses));

        // create a map of addresses and ports
         Map<Integer, String> addressPortMap = new HashMap<>();
         for (int i = 0; i < addresses.length; i++) {
             addressPortMap.put(otherPorts[i], addresses[i]);
         }

        new Thread(() -> new Node(Integer.parseInt(nodeId), portOfTheApp, otherPorts, addressPortMap)).start();
    }
}

