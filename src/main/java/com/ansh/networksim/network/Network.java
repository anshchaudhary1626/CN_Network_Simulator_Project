package com.ansh.networksim.network;

import com.ansh.networksim.model.Bridge;
import com.ansh.networksim.model.Connection;
import com.ansh.networksim.model.Device;
import com.ansh.networksim.model.Switch;

import java.util.*;
import java.util.stream.Collectors;

public class Network {
    private final Map<String , Device> devices;
    private final List<Connection> connections;

    public Network() {
        this.devices = new HashMap<>();
        this.connections = new ArrayList<>();
    }

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

    public void connect(String name1, String name2){
        Device d1 = getDevice(name1);
        Device d2 = getDevice(name2);

        if(d1 == d2){
            throw new IllegalArgumentException("A device cannot be connected to itself: " + d1.getName());
        }

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

    public int countBroadcastDomains() {
        Set<Device> visited = new HashSet<>();
        int domains = 0;

        for (Device device : devices.values()) {
            if (!visited.contains(device)) {
                domains++;
                bfsAllDevices(device, visited);
            }
        }

        return domains;
    }

    public int countCollisionDomains() {
        Set<String> uniqueDomains = new HashSet<>();
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

    private void bfsAllDevices(Device start, Set<Device> visited) {
        ArrayDeque<Device> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Device current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            for (Connection connection : current.getConnections()) {
                queue.add(connection.getOtherDevice(current));
            }
        }
    }

    private Set<Device> bfsCollisionDomain(Connection connection) {
        Set<Device> domain = new HashSet<>();
        ArrayDeque<Device> queue = new ArrayDeque<>();
        queue.add(connection.getDevice1());
        queue.add(connection.getDevice2());

        while (!queue.isEmpty()) {
            Device current = queue.poll();
            if (!domain.add(current)) {
                continue;
            }
            if (isCollisionBoundary(current)) {
                continue;
            }
            for (Connection link : current.getConnections()) {
                queue.add(link.getOtherDevice(current));
            }
        }

        return domain;
    }

    private boolean isCollisionBoundary(Device device) {
        return device instanceof Switch || device instanceof Bridge;
    }
}
