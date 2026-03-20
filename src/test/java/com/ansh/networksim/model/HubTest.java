package com.ansh.networksim.model;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HubTest {

    @Test
    void hubBroadcastsPhysicalPacketToOtherConnections() {
        EndDevice d1 = new EndDevice(1, "D1");
        EndDevice d2 = new EndDevice(2, "D2");
        EndDevice d3 = new EndDevice(3, "D3");
        Hub hub = new Hub(100, "H1");

        Connection c1 = new Connection(d1, hub);
        Connection c2 = new Connection(d2, hub);
        Connection c3 = new Connection(d3, hub);

        d1.addConnection(c1);
        d2.addConnection(c2);
        d3.addConnection(c3);
        hub.addConnection(c1);
        hub.addConnection(c2);
        hub.addConnection(c3);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            d1.send("D2", "hello through hub");
        } finally {
            System.setOut(originalOut);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("H1 received a physical-layer packet and is broadcasting it to all other connections."), printed);
        assertTrue(printed.contains("D2 received the physical-layer message: hello through hub"), printed);
        assertTrue(printed.contains("D3 received a physical-layer packet not meant for it and ignored it."), printed);
    }
}
