package com.ansh.networksim.network;

import com.ansh.networksim.model.Bridge;
import com.ansh.networksim.model.Connection;
import com.ansh.networksim.model.Device;
import com.ansh.networksim.model.Switch;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores the topology and computes summary metrics such as broadcast/collision domains.
 */
public class Network {
    private final Map<String , Device> devices;
    private final List<Connection> connections;

    // Start with an empty topology.
    public Network() {
        this.devices = new HashMap<>();
        this.connections = new ArrayList<>();
    }

    // Device names are used as stable identifiers in the interactive scenarios.
    public void addDevice(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }

        String name = device.getName();

        if (name == null) {
            throw new IllegalArgumentException("Device name cannot be null");
        }

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Device name cannot be blank");
        }

        if (devices.containsKey(name)) {
            throw new IllegalArgumentException("Device with name '" + name + "' already exists");
        }

        devices.put(name, device);
    }

    // Look up a device by its unique name.
    public Device getDevice(String name){
        if(name == null){
            throw new IllegalArgumentException("Device name cannot be null");
        }
        if(name.trim().isEmpty()){
            throw new IllegalArgumentException("Device name cannot be blank");
        }
        if(!devices.containsKey(name)){
            throw new IllegalArgumentException("Device not found: " + name);
        }
        return devices.get(name);
    }

    // Add a bidirectional link between two distinct existing devices.
    public void connect(String name1, String name2){
        Device d1 = getDevice(name1);
        Device d2 = getDevice(name2);

        if(d1 == d2){
            throw new IllegalArgumentException("A device cannot be connected to itself: " + d1.getName());
        }

        // Reject duplicate links regardless of endpoint order.
        for(Connection connection: connections){
            Device existingDevice1 = connection.getDevice1();
            Device existingDevice2 = connection.getDevice2();

            if((existingDevice1 == d1 && existingDevice2 == d2) || (existingDevice1 == d2 && existingDevice2 == d1)){
                throw new IllegalArgumentException("Connection already exists");
            }
        }

        Connection connection = new Connection(d1, d2);

        d1.addConnection(connection);
        d2.addConnection(connection);
        connections.add(connection);

        System.out.println("Connected " + name1 + " <--> " + name2);
    }

    // Print a readable summary of all links in the current topology.
    public void printTopology() {
        System.out.println("\n--- Network Topology ---");
        System.out.println("Devices: " + devices.size());
        System.out.println("Connections: " + connections.size());

        if (connections.isEmpty()) {
            System.out.println("No connections in the network.");
        } else {
            for (Connection connection : connections) {
                System.out.println(connection);
            }
        }

        System.out.println("------------------------\n");
    }

    // In this simulator, every directly or indirectly connected component is one broadcast domain.
    public int countBroadcastDomains() {
        Set<Device> visited = new HashSet<>();
        int domains = 0;

        // Start a traversal for each connected component that has not been visited yet.
        for (Device device : devices.values()) {
            if (!visited.contains(device)) {
                domains++;
                bfsAllDevices(device, visited);
            }
        }

        return domains;
    }

    // Switches and bridges terminate collision domains, so BFS stops when it reaches them.
    public int countCollisionDomains() {
        Set<String> uniqueDomains = new HashSet<>();
        // Build one normalized identifier per collision domain so duplicates collapse.
        for (Connection connection : connections) {
            Set<Device> domain = bfsCollisionDomain(connection);
            String key = domain.stream()
                    .map(Device::getName)
                    .sorted()
                    .collect(Collectors.joining("|"));
            uniqueDomains.add(key);
        }
        return uniqueDomains.size();
    }

    // Traverse the full connected component to mark one broadcast domain.
    private void bfsAllDevices(Device start, Set<Device> visited) {
        ArrayDeque<Device> queue = new ArrayDeque<>();
        queue.add(start);
        // Expand the component breadth-first across every connection.
        while (!queue.isEmpty()) {
            Device current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            // Follow every adjacent connection to continue the traversal.
            for (Connection connection : current.getConnections()) {
                queue.add(connection.getOtherDevice(current));
            }
        }
    }

    // Traverse outward from one link until a collision boundary device is reached.
    private Set<Device> bfsCollisionDomain(Connection connection) {
        Set<Device> domain = new HashSet<>();
        ArrayDeque<Device> queue = new ArrayDeque<>();
        queue.add(connection.getDevice1());
        queue.add(connection.getDevice2());

        // Explore all devices that still share the same collision domain.
        while (!queue.isEmpty()) {
            Device current = queue.poll();
            if (!domain.add(current)) {
                continue;
            }
            if (isCollisionBoundary(current)) {
                continue;
            }
            // Continue only through devices that do not break collision domains.
            for (Connection link : current.getConnections()) {
                queue.add(link.getOtherDevice(current));
            }
        }

        return domain;
    }

    // Switches and bridges divide collision domains in this model.
    private boolean isCollisionBoundary(Device device) {
        return device instanceof Switch || device instanceof Bridge;
    }
}
