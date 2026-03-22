package com.ansh.networksim.simulation;

import com.ansh.networksim.datalink.Frame;
import com.ansh.networksim.datalink.TransmissionRequest;

import java.util.List;

public final class TransmissionTraceRenderer {
    private static final String JAM_SIGNAL = "10101010";

    private TransmissionTraceRenderer() {
    }

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
        for (int index = 0; index < payloadBits.length(); index++) {
            System.out.println("Tick " + tick + ": " + actor + " -> " + peer
                    + " payload bit " + (index + 1) + "/" + payloadBits.length()
                    + " = " + payloadBits.charAt(index));
            tick++;
        }

        return tick;
    }

    public static int renderCollision(String mediumName, List<TransmissionRequest> contenders, int startTick) {
        int overlappingBits = 8;
        int tick = startTick;

        System.out.println("Collision trace on " + mediumName + ":");
        for (int index = 0; index < overlappingBits; index++) {
            StringBuilder line = new StringBuilder("Tick " + tick + ": ");
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

        for (TransmissionRequest contender : contenders) {
            System.out.println(contender.getSender().getName() + " detected the collision and is sending a jam signal.");
        }

        for (int index = 0; index < JAM_SIGNAL.length(); index++) {
            System.out.println("Tick " + tick + ": jam signal bit " + (index + 1) + "/" + JAM_SIGNAL.length()
                    + " = " + JAM_SIGNAL.charAt(index));
            tick++;
        }

        return tick;
    }
}
