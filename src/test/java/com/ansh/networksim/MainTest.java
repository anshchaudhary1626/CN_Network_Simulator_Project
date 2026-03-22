package com.ansh.networksim;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void mainRunsAllScenarioHeadings() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        System.setIn(new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8)));
        try {
            Main.main(new String[0]);
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("===== BASIC PHYSICAL TRANSMISSION ====="), printed);
        assertTrue(printed.contains("===== HUB STAR TOPOLOGY ====="), printed);
        assertTrue(printed.contains("===== SWITCH LEARNING SCENARIO ====="), printed);
        assertTrue(printed.contains("===== CSMA/CD SCENARIO ====="), printed);
        assertTrue(printed.contains("===== GO-BACK-N SCENARIO ====="), printed);
        assertTrue(printed.contains("===== COMBINED TOPOLOGY SCENARIO ====="), printed);
    }

    @Test
    void mainCanRunVisualizationOnlyMode() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            Main.main(new String[]{"visualize"});
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("===== BIT/BY-FRAME VISUALIZATION MODE ====="), printed);
        assertTrue(printed.contains("===== CSMA/CD SCENARIO ====="), printed);
        assertTrue(printed.contains("===== GO-BACK-N SCENARIO ====="), printed);
    }

    @Test
    void mainCanRunSingleScenarioByName() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            Main.main(new String[]{"csma"});
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("===== CSMA/CD SCENARIO ====="), printed);
        assertTrue(!printed.contains("===== GO-BACK-N SCENARIO ====="), printed);
        assertTrue(!printed.contains("===== BASIC PHYSICAL TRANSMISSION ====="), printed);
    }

    @Test
    void mainPromptsForScenarioSelectionWhenNoArgsAreProvided() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        System.setIn(new ByteArrayInputStream("csma\n".getBytes(StandardCharsets.UTF_8)));
        try {
            Main.main(new String[0]);
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Available scenarios: all, visualize, physical, hub, switch, csma, gobackn, combined"), printed);
        assertTrue(printed.contains("Enter scenario:"), printed);
        assertTrue(printed.contains("===== CSMA/CD SCENARIO ====="), printed);
        assertTrue(!printed.contains("===== GO-BACK-N SCENARIO ====="), printed);
    }

    @Test
    void mainCanRunCustomPhysicalScenarioWithBitPayload() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        String scriptedInput = String.join("\n",
                "custom",
                "physical",
                "2",
                "D1",
                "D2",
                "bits",
                "10101010"
        ) + "\n";
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        System.setIn(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        try {
            Main.main(new String[0]);
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }

        String printed = output.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("===== CUSTOM INTERACTIVE SCENARIO ====="), printed);
        assertTrue(printed.contains("Available devices: D1, D2"), printed);
        assertTrue(printed.contains("D1 is sending a physical-layer packet to D2 with payload: 10101010"), printed);
        assertTrue(printed.contains("D2 received the physical-layer message: 10101010"), printed);
    }
}
