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
        assertTrue(printed.contains("timed out and is retransmitting from sequence number 2."), printed);
        assertTrue(printed.contains("Total retransmissions: 1"), printed);
        assertTrue(printed.contains("Transfer completed successfully."), printed);
    }
}
