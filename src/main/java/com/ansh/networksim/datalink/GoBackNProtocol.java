package com.ansh.networksim.datalink;

import com.ansh.networksim.model.EndDevice;
import com.ansh.networksim.simulation.PayloadUtil;
import com.ansh.networksim.simulation.TransmissionTraceRenderer;

import java.util.Random;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GoBackNProtocol {
    private static final int MIN_TIMEOUT_TICKS = 90;
    private static final int MAX_TIMEOUT_TICKS = 150;
    private final int windowSize;
    private final Random random;

    public GoBackNProtocol(int windowSize) {
        this(windowSize, new Random(42));
    }

    public GoBackNProtocol(int windowSize, Random random) {
        this.windowSize = windowSize;
        this.random = random;
    }

    public void transmit(EndDevice sender, EndDevice receiver, List<String> payloads, ErrorInjector injector) {
        if (payloads.isEmpty()) {
            System.out.println("\n--- Go-Back-N Sliding Window ---");
            System.out.println("No payloads to transmit.");
            System.out.println("--- End Go-Back-N ---\n");
            return;
        }

        int base = 0;
        int expectedSeq = 0;
        int tick = 0;

        int sentFrames = 0;
        int retransmissions = 0;
        int ackCount = 0;
        boolean[] transmittedBefore = new boolean[payloads.size()];
        Map<Integer, Integer> sendTickBySequence = new HashMap<>();
        Map<Integer, Integer> timeoutDurationBySequence = new HashMap<>();

        System.out.println("\n--- Go-Back-N Sliding Window ---");
        System.out.println("Sender: " + sender.getName() + ", Receiver: " + receiver.getName() + ", Window Size: " + windowSize);

        while (base < payloads.size()) {
            int windowEnd = Math.min(base + windowSize, payloads.size());
            int windowBase = base;
            System.out.println("Window send range: [" + base + ", " + (windowEnd - 1) + "]");

            for (int nextSeq = base; nextSeq < windowEnd; nextSeq++) {
                Frame frame = Frame.createDataFrame(sender.getName(), receiver.getName(), nextSeq, payloads.get(nextSeq));
                Frame transmitted = injector.maybeCorrupt(frame);
                sendTickBySequence.put(nextSeq, tick);
                int timeoutDuration = sampleTimeoutTicks();
                timeoutDurationBySequence.put(nextSeq, timeoutDuration);
                System.out.println("Timer started for frame " + nextSeq + " at tick " + tick
                        + " with timeout duration " + timeoutDuration
                        + " and timeout threshold tick " + (tick + timeoutDuration) + ".");
                sentFrames++;
                if (transmittedBefore[nextSeq]) {
                    retransmissions++;
                } else {
                    transmittedBefore[nextSeq] = true;
                }
                System.out.println(sender.getName() + " sent frame with sequence number "
                        + transmitted.getSequenceNumber() + " and payload: " + PayloadUtil.display(transmitted.getPayload()));
                tick = TransmissionTraceRenderer.renderFrameFlow(
                        sender.getName(),
                        receiver.getName(),
                        transmitted,
                        tick
                );

                if (!transmitted.isValid()) {
                    System.out.println(receiver.getName() + " detected an error in frame with sequence number "
                            + transmitted.getSequenceNumber() + ".");
                    if (expectedSeq > 0) {
                        System.out.println(receiver.getName() + " repeated ACK for sequence number " + (expectedSeq - 1) + ".");
                        Frame repeatedAck = Frame.createAckFrame(receiver.getName(), sender.getName(), expectedSeq - 1);
                        tick = TransmissionTraceRenderer.renderFrameFlow(
                                receiver.getName(),
                                sender.getName(),
                                repeatedAck,
                                tick
                        );
                    } else {
                        System.out.println(receiver.getName() + " has no valid in-order frame to acknowledge yet.");
                    }
                    continue;
                }

                if (transmitted.getSequenceNumber() == expectedSeq) {
                    System.out.println(receiver.getName() + " accepted frame with sequence number "
                            + transmitted.getSequenceNumber() + ".");
                    expectedSeq++;
                    ackCount++;
                    System.out.println(receiver.getName() + " sent ACK for sequence number " + (expectedSeq - 1) + ".");
                    Frame ack = Frame.createAckFrame(receiver.getName(), sender.getName(), expectedSeq - 1);
                    tick = TransmissionTraceRenderer.renderFrameFlow(
                            receiver.getName(),
                            sender.getName(),
                            ack,
                            tick
                    );
                    System.out.println("ACK for frame " + (expectedSeq - 1) + " arrived at tick " + tick + ".");
                } else {
                    System.out.println(receiver.getName() + " discarded out-of-order frame with sequence number "
                            + transmitted.getSequenceNumber() + ".");
                    System.out.println(receiver.getName() + " repeated ACK for sequence number " + (expectedSeq - 1) + ".");
                    Frame repeatedAck = Frame.createAckFrame(receiver.getName(), sender.getName(), expectedSeq - 1);
                    tick = TransmissionTraceRenderer.renderFrameFlow(
                            receiver.getName(),
                            sender.getName(),
                            repeatedAck,
                            tick
                    );
                }
            }

            if (expectedSeq == windowBase) {
                int timeoutTick = sendTickBySequence.getOrDefault(base, tick)
                        + timeoutDurationBySequence.getOrDefault(base, MIN_TIMEOUT_TICKS);
                if (tick < timeoutTick) {
                    System.out.println("Waiting for timeout on frame " + base + " until tick " + timeoutTick + ".");
                    tick = timeoutTick;
                }
                System.out.println(sender.getName() + " timed out at tick " + tick
                        + " and is retransmitting from sequence number " + base + ".");
            } else if (expectedSeq < payloads.size() && expectedSeq < windowEnd) {
                int timeoutTick = sendTickBySequence.getOrDefault(expectedSeq, tick)
                        + timeoutDurationBySequence.getOrDefault(expectedSeq, MIN_TIMEOUT_TICKS);
                if (tick < timeoutTick) {
                    System.out.println("Waiting for timeout on frame " + expectedSeq + " until tick " + timeoutTick + ".");
                    tick = timeoutTick;
                }
                System.out.println(sender.getName() + " received cumulative ACKs up to sequence number "
                        + (expectedSeq - 1) + " and will go back to " + expectedSeq + " after timeout at tick " + tick + ".");
            }

            base = expectedSeq;
        }

        System.out.println("Total frames sent: " + sentFrames);
        System.out.println("Total retransmissions: " + retransmissions);
        System.out.println("Total ACKs received: " + ackCount);
        System.out.println("Transfer completed successfully.");

        System.out.println("--- End Go-Back-N ---\n");
    }

    private int sampleTimeoutTicks() {
        return MIN_TIMEOUT_TICKS + random.nextInt(MAX_TIMEOUT_TICKS - MIN_TIMEOUT_TICKS + 1);
    }
}
