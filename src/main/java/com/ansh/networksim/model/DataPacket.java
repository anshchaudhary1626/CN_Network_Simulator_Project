package com.ansh.networksim.model;

public class DataPacket {
    private final String source;
    private final String destination;
    private final String payload;

    public DataPacket(String source, String destination, String payload) {
        this.source = source;
        this.destination = destination;
        this.payload = payload;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
