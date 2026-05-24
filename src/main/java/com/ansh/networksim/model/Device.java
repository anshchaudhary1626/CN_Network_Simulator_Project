package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * Base abstraction shared by end devices and forwarding devices.
 */
public abstract class Device {
    private final int id;
    private final String name;
    private final List<Connection> connections;

    // Create a named device with an initially empty list of links.
    public Device(int id, String name) {
        this.id = id;
        this.name = name;
        this.connections = new ArrayList<>();
    }

    // Return the numeric identifier used by the demo.
    public int getId() {
        return id;
    }

    // Return the device name used in logs and addressing.
    public String getName() {
        return name;
    }

    // Return the connections currently attached to this device.
    public List<Connection> getConnections() {
        return connections;
    }

    // Register a new link on this device.
    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    // Subclasses decide how they handle physical-layer packets.
    public abstract void receive(DataPacket packet, Connection fromConnection);

    // Subclasses decide how they handle data-link frames.
    public abstract void receiveFrame(Frame frame, Connection fromConnection);

    // Render the base device information.
    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
