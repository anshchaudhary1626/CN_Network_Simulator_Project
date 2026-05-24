package com.ansh.networksim.model;

/**
 * Simple physical-layer packet used in the direct and hub-based demos.
 */
public class DataPacket {
    private final String source;
    private final String destination;
    private final String payload;

    // Create a physical-layer packet with source, destination, and payload text.
    public DataPacket(String source, String destination, String payload) {
        this.source = source;
        this.destination = destination;
        this.payload = payload;
    }

    // Return the packet source.
    public String getSource() {
        return source;
    }

    // Return the packet destination.
    public String getDestination() {
        return destination;
    }

    // Return the packet payload.
    public String getPayload() {
        return payload;
    }

    // Render the packet fields for debugging.
    @Override
    public String toString() {
        return "DataPacket{" +
                "source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
