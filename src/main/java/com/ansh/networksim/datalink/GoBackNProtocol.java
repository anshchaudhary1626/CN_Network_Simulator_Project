package com.ansh.networksim.datalink;

import com.ansh.networksim.model.EndDevice;
import com.ansh.networksim.simulation.PayloadUtil;
import com.ansh.networksim.simulation.TransmissionTraceRenderer;

import java.util.Random;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * This class shows how Go-Back-N works.
 * The sender can send a group of frames in one window.
 * If one frame is damaged or missed, the sender goes back and sends again from that point.
 */
public class GoBackNProtocol {
    private static final int MIN_TIMEOUT_TICKS = 90;
    private static final int MAX_TIMEOUT_TICKS = 150;
    private final int windowSize;
    private final Random random;

    // We use a fixed random seed so the timeout values look the same in every demo run.
    public GoBackNProtocol(int windowSize) {
        this(windowSize, new Random(42));
    }

    // This version lets us pass our own random generator if we want.
    public GoBackNProtocol(int windowSize, Random random) {
        this.windowSize = windowSize;
        this.random = random;
    }

    // This method runs the full Go-Back-N transfer.
    // It sends frames, checks ACKs, handles corruption, and retransmits after timeout.
    public void transmit(EndDevice sender, EndDevice receiver, List<String> payloads, ErrorInjector injector) {
        // If there is no data at all, we stop immediately.
        if (payloads.isEmpty()) {
            System.out.println("\n--- Go-Back-N Sliding Window ---");
            System.out.println("No payloads to transmit.");
            System.out.println("--- End Go-Back-N ---\n");
            return;
        }

        // 'base' means the first frame that has not been fully confirmed yet.
        int base = 0;

        // 'expectedSeq' means the next frame number the receiver is waiting for.
        int expectedSeq = 0;

        // 'tick' is the running time used only for the printed simulation trace.
        int tick = 0;

        // These counters are only for summary output at the end.
        int sentFrames = 0;
        int retransmissions = 0;
        int ackCount = 0;

        // This remembers whether a given frame was already sent earlier.
        boolean[] transmittedBefore = new boolean[payloads.size()];

        // This stores when each frame was last sent.
        Map<Integer, Integer> sendTickBySequence = new HashMap<>();

        // This stores the timeout length chosen for each frame.
        Map<Integer, Integer> timeoutDurationBySequence = new HashMap<>();

        System.out.println("\n--- Go-Back-N Sliding Window ---");
        System.out.println("Sender: " + sender.getName() + ", Receiver: " + receiver.getName() + ", Window Size: " + windowSize);

        // Keep going until the sender has successfully moved past the last frame.
        while (base < payloads.size()) {
            // The end of the current sender window is limited by the window size
            // and also by how many payload frames actually exist.
            int windowEnd = Math.min(base + windowSize, payloads.size());
            int windowBase = base;
            System.out.println("Window send range: [" + base + ", " + (windowEnd - 1) + "]");

            // Send every frame that currently belongs to the sender window.
            for (int nextSeq = base; nextSeq < windowEnd; nextSeq++) {
                // Build a normal frame for this sequence number.
                Frame frame = Frame.createDataFrame(sender.getName(), receiver.getName(), nextSeq, payloads.get(nextSeq));

                // Corrupt the frame only if the demo was configured to damage this sequence number once.
                Frame transmitted = injector.maybeCorrupt(frame);

                // Remember the moment when this frame was sent.
                sendTickBySequence.put(nextSeq, tick);

                // Give this frame a timeout value for the demo.
                int timeoutDuration = sampleTimeoutTicks();
                timeoutDurationBySequence.put(nextSeq, timeoutDuration);
                System.out.println("Timer started for frame " + nextSeq + " at tick " + tick
                        + " with timeout duration " + timeoutDuration
                        + " and timeout threshold tick " + (tick + timeoutDuration) + ".");
                sentFrames++;

                // If we have already sent this sequence number before, count it as a retransmission.
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

                // If the frame is corrupted, the receiver rejects it.
                // So the sender will not be able to move ahead normally.
                if (!transmitted.isValid()) {
                    System.out.println(receiver.getName() + " detected an error in frame with sequence number "
                            + transmitted.getSequenceNumber() + ".");

                    // The receiver repeats the last good ACK.
                    // In simple words, it is saying:
                    // "I am still waiting for the next correct frame."
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

                // If the receiver got exactly the frame it was waiting for,
                // it accepts the frame and moves forward.
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
                    // If a later frame comes before the missing one,
                    // the receiver does not keep it.
                    // It throws that frame away and repeats the old ACK.
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

            // If the receiver made no progress in this whole window,
            // the sender times out from the base frame and starts again there.
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
                // If some earlier frames were accepted but then progress stopped,
                // the sender goes back to the first missing frame after timeout.
                int timeoutTick = sendTickBySequence.getOrDefault(expectedSeq, tick)
                        + timeoutDurationBySequence.getOrDefault(expectedSeq, MIN_TIMEOUT_TICKS);
                if (tick < timeoutTick) {
                    System.out.println("Waiting for timeout on frame " + expectedSeq + " until tick " + timeoutTick + ".");
                    tick = timeoutTick;
                }
                System.out.println(sender.getName() + " received cumulative ACKs up to sequence number "
                        + (expectedSeq - 1) + " and will go back to " + expectedSeq + " after timeout at tick " + tick + ".");
            }

            // Move the sender window so that it starts from the next frame still waiting for success.
            base = expectedSeq;
        }

        // Print a final summary after all frames have been delivered successfully.
        System.out.println("Total frames sent: " + sentFrames);
        System.out.println("Total retransmissions: " + retransmissions);
        System.out.println("Total ACKs received: " + ackCount);
        System.out.println("Transfer completed successfully.");

        System.out.println("--- End Go-Back-N ---\n");
    }

    // Pick a timeout value used in the demo to decide when retransmission should happen.
    private int sampleTimeoutTicks() {
        return MIN_TIMEOUT_TICKS + random.nextInt(MAX_TIMEOUT_TICKS - MIN_TIMEOUT_TICKS + 1);
    }
}
