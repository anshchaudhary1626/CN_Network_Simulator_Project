package com.ansh.networksim.transport;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoBackNTransportTest {
    @Test
    void retransmitsFromLostSegment() {
        GoBackNTransport goBackN = new GoBackNTransport(3);

        List<TransportSegment> delivered = goBackN.sendSegments(49152, 80, List.of("A", "B", "C", "D"), 1);

        assertTrue(goBackN.didRetransmit());
        assertEquals(List.of(0, 1, 2, 3), delivered.stream().map(TransportSegment::getSequenceNumber).toList());
    }
}
