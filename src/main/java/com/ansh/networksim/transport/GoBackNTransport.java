package com.ansh.networksim.transport;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulates Go-Back-N at the transport layer.
 *
 * In simple words:
 * - Sender can send a small window of segments before waiting.
 * - Receiver sends ACKs for received segments.
 * - If one segment is lost, sender goes back and resends from that lost segment.
 *
 * This is similar to reliability behavior taught in transport-layer protocols.
 */
public class GoBackNTransport {
    private final int windowSize;
    private int senderBase;
    private int nextSequenceNumber;
    private boolean retransmissionOccurred;

    public GoBackNTransport(int windowSize) {
        // Window size controls how many unacknowledged segments can be in flight.
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }
        this.windowSize = windowSize;
    }

    public List<TransportSegment> sendSegments(int sourcePort, int destinationPort, List<String> payloads, int lostSequenceNumber) {
        // Reset sender state for this one transmission.
        senderBase = 0;
        nextSequenceNumber = 0;
        retransmissionOccurred = false;
        List<TransportSegment> delivered = new ArrayList<>();

        while (senderBase < payloads.size()) {
            // Current sending window is from senderBase to windowEnd - 1.
            int windowEnd = Math.min(senderBase + windowSize, payloads.size());
            System.out.println("[TRANSPORT] Go-Back-N window: base=" + senderBase + ", nextSeq=" + nextSequenceNumber
                    + ", allowed=" + senderBase + ".." + (windowEnd - 1));

            boolean lossInWindow = false;
            int firstLost = -1;
            for (int sequence = senderBase; sequence < windowEnd; sequence++) {
                // Convert each payload chunk into a transport segment.
                TransportSegment segment = TransportSegment.data(sourcePort, destinationPort, sequence, payloads.get(sequence));
                System.out.println("[TRANSPORT] TCP-like Go-Back-N segment created: seq=" + sequence
                        + ", srcPort=" + sourcePort + ", destPort=" + destinationPort);
                nextSequenceNumber = sequence + 1;
                if (!retransmissionOccurred && sequence == lostSequenceNumber) {
                    // Drop one chosen segment to demonstrate timeout and retransmission.
                    System.out.println("[TRANSPORT] Simulated loss for segment seq=" + sequence);
                    lossInWindow = true;
                    firstLost = sequence;
                    break;
                }
                delivered.add(segment);
                // In this simplified model, a non-lost segment is immediately acknowledged.
                System.out.println("[TRANSPORT] ACK received for seq=" + sequence);
            }

            if (lossInWindow) {
                // Go-Back-N rule: after loss, resend from the first lost segment.
                retransmissionOccurred = true;
                System.out.println("[TRANSPORT] Timeout detected. Retransmitting from seq=" + firstLost);
                senderBase = firstLost;
            } else {
                // If every segment in the window was ACKed, slide the window forward.
                senderBase = windowEnd;
                System.out.println("[TRANSPORT] Window moved to base=" + senderBase);
            }
        }

        return delivered;
    }

    public boolean didRetransmit() {
        return retransmissionOccurred;
    }
}
