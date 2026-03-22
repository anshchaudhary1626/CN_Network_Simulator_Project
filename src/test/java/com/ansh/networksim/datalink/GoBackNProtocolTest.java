package com.ansh.networksim.datalink;

import com.ansh.networksim.model.EndDevice;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GoBackNProtocolTest {

    @Test
    void transmitReportsSuccessfulRecoveryAfterInjectedError() {
        GoBackNProtocol protocol = new GoBackNProtocol(3);
        EndDevice sender = new EndDevice(1, "S1");
        EndDevice receiver = new EndDevice(2, "S2");
        ErrorInjector injector = new ErrorInjector(new HashSet<>(List.of(2)));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            protocol.transmit(sender, receiver, List.of("P0", "P1", "P2", "P3", "P4"), injector);
        } finally {
            System.setOut(originalOut);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Payload bits: 0101000000110000"), printed);
        assertTrue(printed.contains("Tick 0: S1 -> S2 payload bit 1/16 = 0"), printed);
        assertTrue(printed.contains("Window send range: [0, 2]"), printed);
        assertTrue(printed.contains("Timer started for frame 0 at tick 0 with timeout duration "), printed);
        assertTrue(printed.contains("and timeout threshold tick "), printed);
        assertTrue(printed.contains("prepared ACK frame seq=0 for S1."), printed);
        assertTrue(printed.contains("Payload bits: <none>"), printed);
        assertTrue(printed.contains("ACK for frame 0 arrived at tick "), printed);
        assertTrue(printed.contains("received cumulative ACKs up to sequence number 1 and will go back to 2 after timeout at tick "), printed);
        assertTrue(printed.contains("detected an error in frame with sequence number 2."), printed);
        assertTrue(printed.contains("Total frames sent: 6"), printed);
        assertTrue(printed.contains("Total retransmissions: 1"), printed);
        assertTrue(printed.contains("Transfer completed successfully."), printed);
    }
}
