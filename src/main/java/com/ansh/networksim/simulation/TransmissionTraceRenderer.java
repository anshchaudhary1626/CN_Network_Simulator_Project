package com.ansh.networksim.simulation;

import com.ansh.networksim.datalink.Frame;
import com.ansh.networksim.datalink.TransmissionRequest;

import java.util.List;

/**
 * Prints bit-by-bit traces for successful transmissions and collisions.
 */
public final class TransmissionTraceRenderer {
    private static final String JAM_SIGNAL = "10101010";

    // Prevent instantiation of this tracing helper.
    private TransmissionTraceRenderer() {
    }

    // ACKs produce no payload bits, so the trace stops after printing the header line.
    public static int renderFrameFlow(String actor, String peer, Frame frame, int startTick) {
        System.out.println(actor + " prepared " + frame.getType() + " frame seq=" + frame.getSequenceNumber()
                + " for " + peer + ".");
        String payloadBits = PayloadUtil.toPayloadBits(frame.getPayload());
        if (payloadBits.isEmpty()) {
            System.out.println("Payload bits: <none>");
            return startTick;
        }

        System.out.println("Payload bits: " + payloadBits);

        int tick = startTick;
        // Print each payload bit in order to simulate time passing on the medium.
        for (int index = 0; index < payloadBits.length(); index++) {
            System.out.println("Tick " + tick + ": " + actor + " -> " + peer
                    + " payload bit " + (index + 1) + "/" + payloadBits.length()
                    + " = " + payloadBits.charAt(index));
            tick++;
        }

        return tick;
    }

    // Collisions are visualized with a short overlap plus a jam signal rather than the full original frames.
    public static int renderCollision(String mediumName, List<TransmissionRequest> contenders, int startTick) {
        int overlappingBits = 8;
        int tick = startTick;

        System.out.println("Collision trace on " + mediumName + ":");
        // Show a short overlapping section of all contenders to visualize the collision.
        for (int index = 0; index < overlappingBits; index++) {
            StringBuilder line = new StringBuilder("Tick " + tick + ": ");
            // Add one payload bit from each contender to the same collision line.
            for (int contenderIndex = 0; contenderIndex < contenders.size(); contenderIndex++) {
                TransmissionRequest contender = contenders.get(contenderIndex);
                String payloadBits = PayloadUtil.toPayloadBits(contender.getFrame().getPayload());
                line.append(contender.getSender().getName())
                        .append(" sent payload bit ")
                        .append(payloadBits.charAt(index % payloadBits.length()));
                if (contenderIndex < contenders.size() - 1) {
                    line.append(", ");
                }
            }
            line.append(" -> collision on medium");
            System.out.println(line);
            tick++;
        }

        // Announce jam-signal transmission from each colliding node.
        for (TransmissionRequest contender : contenders) {
            System.out.println(contender.getSender().getName() + " detected the collision and is sending a jam signal.");
        }

        // Print the shared jam signal bits that mark the collision on the medium.
        for (int index = 0; index < JAM_SIGNAL.length(); index++) {
            System.out.println("Tick " + tick + ": jam signal bit " + (index + 1) + "/" + JAM_SIGNAL.length()
                    + " = " + JAM_SIGNAL.charAt(index));
            tick++;
        }

        return tick;
    }
}
