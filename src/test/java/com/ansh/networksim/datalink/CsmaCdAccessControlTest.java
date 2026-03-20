package com.ansh.networksim.datalink;

import com.ansh.networksim.model.Connection;
import com.ansh.networksim.model.EndDevice;
import com.ansh.networksim.model.Hub;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CsmaCdAccessControlTest {

    @Test
    void simulateSlotHandlesCollisionAcrossBackoffRounds() {
        EndDevice s1 = new EndDevice(1, "S1");
        EndDevice s2 = new EndDevice(2, "S2");
        Hub hub = new Hub(100, "H1");

        Connection c1 = new Connection(s1, hub);
        Connection c2 = new Connection(s2, hub);
        s1.addConnection(c1);
        s2.addConnection(c2);
        hub.addConnection(c1);
        hub.addConnection(c2);

        TransmissionRequest t1 = new TransmissionRequest(s1, c1, Frame.createDataFrame("S1", "S2", 0, "one"));
        TransmissionRequest t2 = new TransmissionRequest(s2, c2, Frame.createDataFrame("S2", "S1", 0, "two"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            new CsmaCdAccessControl().simulateSlot(new SharedMedium(), List.of(t1, t2));
        } finally {
            System.setOut(originalOut);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Collision detected for 2 simultaneous transmissions."), printed);
        assertTrue(printed.contains("Backoff round 1:"), printed);
        assertTrue(printed.contains("Backoff round 2:"), printed);
        assertTrue(printed.contains("S2 must wait for the next backoff round."), printed);
    }
}
