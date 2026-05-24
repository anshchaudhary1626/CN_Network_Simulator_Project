package com.ansh.networksim.network;

/**
 * Represents the network-layer packet.
 *
 * In the full stack:
 * - Application creates data.
 * - Transport wraps it in a segment.
 * - Network wraps the segment in an IPv4 packet.
 * - Data link wraps the packet in a frame.
 *
 * This class stores the packet fields needed for routing.
 */
public class IPv4Packet {
    private final String sourceIp;
    private final String destinationIp;
    private final int ttl;
    private final String protocol;
    private final String payload;

    public IPv4Packet(String sourceIp, String destinationIp, int ttl, String protocol, String payload) {
        // Store the main IPv4 header fields and the payload carried inside the packet.
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.ttl = ttl;
        this.protocol = protocol;
        this.payload = payload;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public int getTtl() {
        return ttl;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getPayload() {
        return payload;
    }

    public IPv4Packet decrementTtl() {
        // Each router hop reduces TTL. If TTL reaches zero, the packet should be dropped.
        if (ttl <= 0) {
            throw new IllegalStateException("Packet TTL expired");
        }
        return new IPv4Packet(sourceIp, destinationIp, ttl - 1, protocol, payload);
    }

    @Override
    public String toString() {
        return "IPv4Packet{srcIP=" + sourceIp
                + ", destIP=" + destinationIp
                + ", ttl=" + ttl
                + ", protocol=" + protocol
                + ", payload='" + payload + "'}";
    }
}
