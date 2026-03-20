package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;

import java.util.ArrayList;
import java.util.List;

public abstract class Device {
    private final int id;
    private final String name;
    private final List<Connection> connections;

    public Device(int id, String name) {
        this.id = id;
        this.name = name;
        this.connections = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    public abstract void receive(DataPacket packet, Connection fromConnection);

    public abstract void receiveFrame(Frame frame, Connection fromConnection);

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
