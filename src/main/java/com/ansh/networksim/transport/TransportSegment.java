package com.ansh.networksim.transport;

/**
 * Represents a transport-layer segment.
 *
 * In simple words:
 * - The application gives data to the transport layer.
 * - The transport layer adds ports and sequence numbers.
 * - That wrapped data is called a segment.
 *
 * Ports identify processes, and sequence numbers help Go-Back-N order/retry data.
 */
public class TransportSegment {
    private final int sourcePort;
    private final int destinationPort;
    private final int sequenceNumber;
    private final int acknowledgementNumber;
    private final String payload;
    private final SegmentType type;

    public TransportSegment(int sourcePort, int destinationPort, int sequenceNumber,
                            int acknowledgementNumber, String payload, SegmentType type) {
        // Store source/destination process ports and reliability fields.
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.sequenceNumber = sequenceNumber;
        this.acknowledgementNumber = acknowledgementNumber;
        this.payload = payload;
        this.type = type;
    }

    public static TransportSegment data(int sourcePort, int destinationPort, int sequenceNumber, String payload) {
        // Helper for creating normal data segments.
        return new TransportSegment(sourcePort, destinationPort, sequenceNumber, -1, payload, SegmentType.DATA);
    }

    public static TransportSegment ack(int sourcePort, int destinationPort, int acknowledgementNumber) {
        // Helper for creating ACK segments. ACKs do not need payload in this simulation.
        return new TransportSegment(sourcePort, destinationPort, -1, acknowledgementNumber, "", SegmentType.ACK);
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getAcknowledgementNumber() {
        return acknowledgementNumber;
    }

    public String getPayload() {
        return payload;
    }

    public SegmentType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "TransportSegment{srcPort=" + sourcePort
                + ", destPort=" + destinationPort
                + ", seq=" + sequenceNumber
                + ", ack=" + acknowledgementNumber
                + ", type=" + type
                + ", payload='" + payload + "'}";
    }
}
