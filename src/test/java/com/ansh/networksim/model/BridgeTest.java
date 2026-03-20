package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BridgeTest {

    @Test
    void bridgeRejectsMoreThanTwoPorts() {
        Bridge bridge = new Bridge(10, "B1");
        EndDevice d1 = new EndDevice(1, "D1");
        EndDevice d2 = new EndDevice(2, "D2");
        EndDevice d3 = new EndDevice(3, "D3");

        bridge.addConnection(new Connection(d1, bridge));
        bridge.addConnection(new Connection(d2, bridge));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> bridge.addConnection(new Connection(d3, bridge))
        );

        assertEquals("B1 only supports 2 ports ", exception.getMessage());
    }

    @Test
    void bridgePrintsLearnedMacTableAfterTraffic() {
        EndDevice d1 = new EndDevice(1, "D1");
        EndDevice d2 = new EndDevice(2, "D2");
        Bridge bridge = new Bridge(10, "B1");

        Connection c1 = new Connection(d1, bridge);
        Connection c2 = new Connection(d2, bridge);
        d1.addConnection(c1);
        d2.addConnection(c2);
        bridge.addConnection(c1);
        bridge.addConnection(c2);

        bridge.receiveFrame(Frame.createDataFrame("D1", "D2", 0, "hello"), c1);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            bridge.printMacTable();
        } finally {
            System.setOut(originalOut);
        }

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("MAC: D1"));
    }
}
