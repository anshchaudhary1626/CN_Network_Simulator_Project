package com.ansh.networksim;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @Test
    void mainRunsAutomaticFullStackSimulationWithoutMenuPrompt() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            Main.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("===== ITL351 FULL PROTOCOL STACK NETWORK SIMULATION ====="), printed);
        assertTrue(printed.contains("[APP-DNS] Query: www.college.local"), printed);
        assertTrue(printed.contains("[APP-HTTP] GET /index.html"), printed);
        assertTrue(printed.contains("[ROUTER] Router1 performs longest prefix match"), printed);
        assertTrue(printed.contains("[TRANSPORT] Timeout detected. Retransmitting from seq=1"), printed);
        assertFalse(printed.contains("Enter scenario"), printed);
        assertFalse(printed.contains("Choose mode"), printed);
    }
}
