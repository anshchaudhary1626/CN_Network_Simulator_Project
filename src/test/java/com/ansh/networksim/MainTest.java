package com.ansh.networksim;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void mainRunsAllScenarioHeadings() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            Main.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("===== BASIC PHYSICAL TRANSMISSION ====="), printed);
        assertTrue(printed.contains("===== HUB STAR TOPOLOGY ====="), printed);
        assertTrue(printed.contains("===== SWITCH LEARNING SCENARIO ====="), printed);
        assertTrue(printed.contains("===== CSMA/CD SCENARIO ====="), printed);
        assertTrue(printed.contains("===== GO-BACK-N SCENARIO ====="), printed);
        assertTrue(printed.contains("===== COMBINED TOPOLOGY SCENARIO ====="), printed);
    }
}
