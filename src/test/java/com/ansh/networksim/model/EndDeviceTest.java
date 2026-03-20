package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;
import com.ansh.networksim.datalink.FrameType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndDeviceTest {

    @Test
    void sendWithoutConnectionThrows() {
        EndDevice device = new EndDevice(1, "A");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> device.send("B", "Hello")
        );

        assertEquals("A is not connected to any device", exception.getMessage());
    }

    @Test
    void receiveAckPrintsSequenceNumber() {
        EndDevice device = new EndDevice(1, "A");
        Frame ack = new Frame("B", "A", 4, "", 0, FrameType.ACK);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            device.receiveFrame(ack, null);
        } finally {
            System.setOut(originalOut);
        }

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("received ACK for sequence number 4."));
    }
}
