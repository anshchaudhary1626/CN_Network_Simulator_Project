package com.ansh.networksim.datalink;

import com.ansh.networksim.model.EndDevice;

import java.util.List;

public class GoBackNProtocol {
    private final int windowSize;

    public GoBackNProtocol(int windowSize) {
        this.windowSize = windowSize;
    }

    public void transmit(EndDevice sender, EndDevice receiver, List<String> payloads, ErrorInjector injector) {
        int base = 0;
        int nextSeq = 0;
        int expectedSeq = 0;

        int sentFrames = 0;
        int retransmissions = 0;
        int ackCount = 0;

        System.out.println("\n--- Go-Back-N Sliding Window ---");
        System.out.println("Sender: " + sender.getName() + ", Receiver: " + receiver.getName() + ", Window Size: " + windowSize);

        while (base < payloads.size()) {
            while (nextSeq < payloads.size() && nextSeq < base + windowSize) {
                Frame frame = Frame.createDataFrame(sender.getName(), receiver.getName(), nextSeq, payloads.get(nextSeq));
                Frame transmitted = injector.maybeCorrupt(frame);
                sentFrames++;
                System.out.println(sender.getName() + " sent frame with sequence number "
                        + transmitted.getSequenceNumber() + " and payload: " + transmitted.getPayload());

                if (!transmitted.isValid()) {
                    retransmissions++;
                    System.out.println(receiver.getName() + " detected an error in frame with sequence number "
                            + transmitted.getSequenceNumber() + ".");
                    System.out.println(sender.getName() + " timed out and is retransmitting from sequence number " + base + ".");
                    nextSeq = base;
                    break;
                }

                if (transmitted.getSequenceNumber() == expectedSeq) {
                    System.out.println(receiver.getName() + " accepted frame with sequence number "
                            + transmitted.getSequenceNumber() + ".");
                    expectedSeq++;
                    ackCount++;
                    base = expectedSeq;
                    System.out.println(receiver.getName() + " sent ACK for sequence number " + (expectedSeq - 1) + ".");
                    nextSeq++;
                } else {
                    System.out.println(receiver.getName() + " discarded out-of-order frame with sequence number "
                            + transmitted.getSequenceNumber() + ".");
                    System.out.println(receiver.getName() + " repeated ACK for sequence number " + (expectedSeq - 1) + ".");
                    nextSeq = base;
                    break;
                }
            }
        }

        System.out.println("Total frames sent: " + sentFrames);
        System.out.println("Total retransmissions: " + retransmissions);
        System.out.println("Total ACKs received: " + ackCount);
        System.out.println("Transfer completed successfully.");

        System.out.println("--- End Go-Back-N ---\n");
    }
}
