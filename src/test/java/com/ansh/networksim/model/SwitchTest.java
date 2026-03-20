package com.ansh.networksim.model;

import com.ansh.networksim.datalink.Frame;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitchTest {

    @Test
    void switchPrintsLearnedMacTableAfterTraffic() {
        EndDevice s1 = new EndDevice(1, "S1");
        EndDevice s3 = new EndDevice(3, "S3");
        Switch sw = new Switch(200, "SW1");

        Connection c1 = new Connection(s1, sw);
        Connection c3 = new Connection(s3, sw);
        s1.addConnection(c1);
        s3.addConnection(c3);
        sw.addConnection(c1);
        sw.addConnection(c3);

        sw.receiveFrame(Frame.createDataFrame("S3", "S1", 0, "Learning"), c3);
        sw.receiveFrame(Frame.createDataFrame("S1", "S3", 0, "Reply"), c1);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            sw.printMacTable();
        } finally {
            System.setOut(originalOut);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("MAC: S1"), printed);
        assertTrue(printed.contains("MAC: S3"), printed);
    }
}
