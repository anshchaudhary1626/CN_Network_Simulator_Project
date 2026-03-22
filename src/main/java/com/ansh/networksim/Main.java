package com.ansh.networksim;

import com.ansh.networksim.datalink.CsmaCdAccessControl;
import com.ansh.networksim.datalink.ErrorInjector;
import com.ansh.networksim.datalink.Frame;
import com.ansh.networksim.datalink.GoBackNProtocol;
import com.ansh.networksim.datalink.SharedMedium;
import com.ansh.networksim.datalink.TransmissionRequest;
import com.ansh.networksim.model.EndDevice;
import com.ansh.networksim.model.Hub;
import com.ansh.networksim.model.Switch;
import com.ansh.networksim.network.Network;
import com.ansh.networksim.simulation.PayloadUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final String ALL = "all";
    private static final String VISUALIZE = "visualize";
    private static final String PHYSICAL = "physical";
    private static final String HUB = "hub";
    private static final String SWITCH = "switch";
    private static final String CSMA = "csma";
    private static final String GOBACKN = "gobackn";
    private static final String COMBINED = "combined";
    private static final String CUSTOM = "custom";

    public static void main(String[] args) {
        if (args.length == 0) {
            promptAndRunScenario();
            return;
        }

        runScenario(args[0]);
    }

    private static void promptAndRunScenario() {
        Scanner scanner = new Scanner(System.in);
        printUsage();
        System.out.print("Enter scenario: ");
        String selectedScenario = scanner.nextLine().trim();
        if (selectedScenario.isEmpty()) {
            runAllScenarios();
            return;
        }

        if (CUSTOM.equalsIgnoreCase(selectedScenario)) {
            runCustomScenario(scanner);
            return;
        }

        runScenario(selectedScenario);
    }

    private static void runScenario(String scenarioName) {
        String scenario = scenarioName.toLowerCase(Locale.ROOT);
        switch (scenario) {
            case ALL -> runAllScenarios();
            case VISUALIZE -> runVisualizationScenarios();
            case PHYSICAL -> runBasicPhysicalScenario();
            case HUB -> runHubScenario();
            case SWITCH -> runSwitchScenario();
            case CSMA -> runCsmaCdScenario();
            case GOBACKN -> runGoBackNScenario();
            case COMBINED -> runCombinedTopologyScenario();
            case CUSTOM -> runCustomScenario(new Scanner(System.in));
            default -> {
                printUsage();
                throw new IllegalArgumentException("Unknown scenario: " + scenarioName);
            }
        }
    }

    private static void runAllScenarios() {
        runBasicPhysicalScenario();
        runHubScenario();
        runSwitchScenario();
        runCsmaCdScenario();
        runGoBackNScenario();
        runCombinedTopologyScenario();
    }

    private static void runVisualizationScenarios() {
        System.out.println("\n===== BIT/BY-FRAME VISUALIZATION MODE =====");
        System.out.println("Running focused protocol demos with detailed bit traces.");
        runCsmaCdScenario();
        runGoBackNScenario();
    }

    private static void printUsage() {
        System.out.println("Usage: java com.ansh.networksim.Main [all|visualize|physical|hub|switch|csma|gobackn|combined|custom]");
        System.out.println("Available scenarios: all, visualize, physical, hub, switch, csma, gobackn, combined, custom");
        System.out.println("Press Enter to run all scenarios.");
    }

    private static void runCustomScenario(Scanner scanner) {
        System.out.println("\n===== CUSTOM INTERACTIVE SCENARIO =====\n");
        System.out.println("Custom modes: physical, switch, csma, gobackn");

        String mode = readChoice(scanner, "Choose mode", List.of(PHYSICAL, SWITCH, CSMA, GOBACKN));
        int deviceCount = readInt(scanner, "How many end devices do you need? ", 2, 10);

        Network network = new Network();
        Map<String, EndDevice> devices = new LinkedHashMap<>();
        for (int index = 1; index <= deviceCount; index++) {
            EndDevice device = new EndDevice(index, "D" + index);
            network.addDevice(device);
            devices.put(device.getName(), device);
        }

        if (PHYSICAL.equals(mode)) {
            setupPhysicalNetwork(network, devices);
        } else if (SWITCH.equals(mode)) {
            setupSwitchNetwork(network, devices);
        } else if (CSMA.equals(mode)) {
            setupCsmaNetwork(network, devices);
        }

        System.out.println("Available devices: " + String.join(", ", devices.keySet()));
        String senderName = readChoice(scanner, "Enter sender device", List.copyOf(devices.keySet()));
        String receiverName = readChoice(scanner, "Enter receiver device", availableDestinations(devices, senderName));

        String payload = readPayload(scanner);

        switch (mode) {
            case PHYSICAL -> {
                network.printTopology();
                devices.get(senderName).send(receiverName, payload);
            }
            case SWITCH -> {
                network.printTopology();
                devices.get(senderName).sendFrame(receiverName, payload);
            }
            case CSMA -> runCustomCsmaScenario(scanner, network, devices, senderName, receiverName, payload);
            case GOBACKN -> runCustomGoBackNScenario(scanner, senderName, receiverName, payload);
            default -> throw new IllegalStateException("Unsupported custom mode: " + mode);
        }
    }

    private static void setupPhysicalNetwork(Network network, Map<String, EndDevice> devices) {
        if (devices.size() == 2) {
            List<String> names = List.copyOf(devices.keySet());
            network.connect(names.get(0), names.get(1));
            return;
        }

        Hub hub = new Hub(10_000, "H-CUSTOM");
        network.addDevice(hub);
        for (String name : devices.keySet()) {
            network.connect(name, hub.getName());
        }
    }

    private static void setupSwitchNetwork(Network network, Map<String, EndDevice> devices) {
        Switch sw = new Switch(20_000, "SW-CUSTOM");
        network.addDevice(sw);
        for (String name : devices.keySet()) {
            network.connect(name, sw.getName());
        }
    }

    private static void setupCsmaNetwork(Network network, Map<String, EndDevice> devices) {
        Hub hub = new Hub(30_000, "H-CSMA-CUSTOM");
        network.addDevice(hub);
        for (String name : devices.keySet()) {
            network.connect(name, hub.getName());
        }
    }

    private static void runCustomCsmaScenario(Scanner scanner, Network network, Map<String, EndDevice> devices,
                                              String senderName, String receiverName, String payload) {
        network.printTopology();
        SharedMedium medium = new SharedMedium();
        CsmaCdAccessControl csmaCd = new CsmaCdAccessControl();
        EndDevice sender = devices.get(senderName);
        EndDevice receiver = devices.get(receiverName);

        TransmissionRequest primary = new TransmissionRequest(
                sender,
                sender.getConnections().get(0),
                Frame.createDataFrame(senderName, receiverName, 0, payload)
        );

        String collisionChoice = readChoice(scanner, "Collision demo? ", List.of("yes", "no"));
        if ("yes".equals(collisionChoice) && devices.size() > 2) {
            String competingSenderName = readChoice(
                    scanner,
                    "Choose competing sender",
                    availableCompetitors(devices, senderName, receiverName)
            );
            String competingPayload = readPayload(scanner, "Enter competing payload");
            TransmissionRequest competing = new TransmissionRequest(
                    devices.get(competingSenderName),
                    devices.get(competingSenderName).getConnections().get(0),
                    Frame.createDataFrame(competingSenderName, senderName, 0, competingPayload)
            );
            csmaCd.simulateSlot(medium, Arrays.asList(primary, competing));
        } else {
            csmaCd.simulateSlot(medium, List.of(primary));
        }
    }

    private static void runCustomGoBackNScenario(Scanner scanner, String senderName, String receiverName, String payload) {
        EndDevice sender = new EndDevice(1, senderName);
        EndDevice receiver = new EndDevice(2, receiverName);
        int windowSize = readInt(scanner, "Enter Go-Back-N window size: ", 1, 10);
        List<String> payloadFrames = splitPayloadIntoFrames(payload);
        int maxSequence = payloadFrames.size() - 1;
        int errorSequence = readInt(scanner,
                "Enter sequence number to corrupt (-1 for no error, max " + maxSequence + "): ",
                -1,
                maxSequence
        );

        HashSet<Integer> errors = new HashSet<>();
        if (errorSequence >= 0) {
            errors.add(errorSequence);
        }

        GoBackNProtocol goBackN = new GoBackNProtocol(windowSize);
        goBackN.transmit(sender, receiver, payloadFrames, new ErrorInjector(errors));
    }

    private static List<String> splitPayloadIntoFrames(String payload) {
        List<String> frames = new java.util.ArrayList<>();
        if (PayloadUtil.isBitPayload(payload)) {
            String bits = PayloadUtil.display(payload);
            for (int index = 0; index < bits.length(); index += 8) {
                frames.add(PayloadUtil.fromBits(bits.substring(index, Math.min(index + 8, bits.length()))));
            }
            return frames;
        }

        for (int index = 0; index < payload.length(); index += 2) {
            frames.add(payload.substring(index, Math.min(index + 2, payload.length())));
        }
        return frames;
    }

    private static String readPayload(Scanner scanner) {
        return readPayload(scanner, "Enter payload");
    }

    private static String readPayload(Scanner scanner, String prompt) {
        String format = readChoice(scanner, "Payload format", List.of("message", "bits"));
        System.out.print(prompt + ": ");
        String payload = scanner.nextLine().trim();
        while (payload.isEmpty()) {
            System.out.print("Payload cannot be empty. " + prompt + ": ");
            payload = scanner.nextLine().trim();
        }

        if ("bits".equals(format)) {
            while (!payload.matches("[01]+")) {
                System.out.print("Enter only 0 and 1 for bit payload: ");
                payload = scanner.nextLine().trim();
            }
            return PayloadUtil.fromBits(payload);
        }

        return payload;
    }

    private static String readChoice(Scanner scanner, String prompt, List<String> allowedChoices) {
        System.out.print(prompt + " [" + String.join("/", allowedChoices) + "]: ");
        String value = scanner.nextLine().trim();
        while (resolveChoice(value, allowedChoices) == null) {
            System.out.print("Invalid choice. " + prompt + " [" + String.join("/", allowedChoices) + "]: ");
            value = scanner.nextLine().trim();
        }
        return resolveChoice(value, allowedChoices);
    }

    private static int readInt(Scanner scanner, String prompt, int min, int max) {
        System.out.print(prompt);
        while (true) {
            String raw = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    System.out.print("Enter a value between " + min + " and " + max + ": ");
                    continue;
                }
                return value;
            } catch (NumberFormatException ex) {
                System.out.print("Enter a valid number: ");
            }
        }
    }

    private static List<String> availableDestinations(Map<String, EndDevice> devices, String senderName) {
        return devices.keySet().stream()
                .filter(name -> !name.equals(senderName))
                .toList();
    }

    private static List<String> availableCompetitors(Map<String, EndDevice> devices, String senderName, String receiverName) {
        List<String> competitors = devices.keySet().stream()
                .filter(name -> !name.equals(senderName) && !name.equals(receiverName))
                .toList();
        if (competitors.isEmpty()) {
            return availableDestinations(devices, senderName);
        }
        return competitors;
    }

    private static String resolveChoice(String rawInput, List<String> allowedChoices) {
        for (String allowedChoice : allowedChoices) {
            if (allowedChoice.equalsIgnoreCase(rawInput)) {
                return allowedChoice;
            }
        }
        return null;
    }

    private static void runBasicPhysicalScenario() {
        System.out.println("\n===== BASIC PHYSICAL TRANSMISSION =====\n");

        Network network = new Network();
        EndDevice a = new EndDevice(1, "A");
        EndDevice b = new EndDevice(2, "B");

        network.addDevice(a);
        network.addDevice(b);
        network.connect("A", "B");

        network.printTopology();
        a.send("B", "Hello B, it's A this side");
    }

    private static void runHubScenario() {
        System.out.println("\n===== HUB STAR TOPOLOGY =====\n");

        Network network = new Network();

        EndDevice d1 = new EndDevice(1, "D1");
        EndDevice d2 = new EndDevice(2, "D2");
        EndDevice d3 = new EndDevice(3, "D3");
        EndDevice d4 = new EndDevice(4, "D4");
        EndDevice d5 = new EndDevice(5, "D5");
        Hub hub = new Hub(100, "H1");

        network.addDevice(d1);
        network.addDevice(d2);
        network.addDevice(d3);
        network.addDevice(d4);
        network.addDevice(d5);
        network.addDevice(hub);

        network.connect("D1", "H1");
        network.connect("D2", "H1");
        network.connect("D3", "H1");
        network.connect("D4", "H1");
        network.connect("D5", "H1");

        network.printTopology();
        d1.send("D4", "Hola Amigo D4 through hub");
    }

    private static void runSwitchScenario() {
        System.out.println("\n===== SWITCH LEARNING SCENARIO =====\n");

        Network network = new Network();

        EndDevice s1 = new EndDevice(1, "S1");
        EndDevice s2 = new EndDevice(2, "S2");
        EndDevice s3 = new EndDevice(3, "S3");
        EndDevice s4 = new EndDevice(4, "S4");
        EndDevice s5 = new EndDevice(5, "S5");
        Switch sw = new Switch(200, "SW1");

        network.addDevice(s1);
        network.addDevice(s2);
        network.addDevice(s3);
        network.addDevice(s4);
        network.addDevice(s5);
        network.addDevice(sw);

        network.connect("S1", "SW1");
        network.connect("S2", "SW1");
        network.connect("S3", "SW1");
        network.connect("S4", "SW1");
        network.connect("S5", "SW1");

        network.printTopology();
        System.out.println("Broadcast domains: " + network.countBroadcastDomains());
        System.out.println("Collision domains: " + network.countCollisionDomains());

        s3.sendFrame("S1", "Learning frame from S3");
        s1.sendFrame("S3", "First frame (switch learns)");
        s1.sendFrame("S3", "Second frame (unicast after learning)");

        sw.printMacTable();
    }

    private static void runCsmaCdScenario() {
        System.out.println("\n===== CSMA/CD SCENARIO =====\n");

        Network network = new Network();
        EndDevice s1 = new EndDevice(1, "S1");
        EndDevice s2 = new EndDevice(2, "S2");
        Hub hub = new Hub(101, "H-CSMA");

        network.addDevice(s1);
        network.addDevice(s2);
        network.addDevice(hub);

        network.connect("S1", "H-CSMA");
        network.connect("S2", "H-CSMA");

        SharedMedium medium = new SharedMedium();
        CsmaCdAccessControl csmaCd = new CsmaCdAccessControl();

        TransmissionRequest t1 = new TransmissionRequest(
                s1,
                s1.getConnections().get(0),
                Frame.createDataFrame("S1", "S2", 0, "Slot data 1")
        );
        TransmissionRequest t2 = new TransmissionRequest(
                s2,
                s2.getConnections().get(0),
                Frame.createDataFrame("S2", "S1", 0, "Slot data 2")
        );

        csmaCd.simulateSlot(medium, Arrays.asList(t1, t2));
    }

    private static void runGoBackNScenario() {
        System.out.println("\n===== GO-BACK-N SCENARIO =====\n");

        EndDevice sender = new EndDevice(1, "S1");
        EndDevice receiver = new EndDevice(2, "S2");
        GoBackNProtocol goBackN = new GoBackNProtocol(3);
        ErrorInjector injector = new ErrorInjector(new HashSet<>(List.of(2)));

        goBackN.transmit(sender, receiver, List.of("P0", "P1", "P2", "P3", "P4"), injector);
    }

    private static void runCombinedTopologyScenario() {
        System.out.println("\n===== COMBINED TOPOLOGY SCENARIO =====\n");

        Network network = new Network();
        Hub hub1 = new Hub(300, "HUB-A");
        Hub hub2 = new Hub(301, "HUB-B");
        Switch core = new Switch(400, "CORE");

        EndDevice a1 = new EndDevice(11, "A1");
        EndDevice a2 = new EndDevice(12, "A2");
        EndDevice a3 = new EndDevice(13, "A3");
        EndDevice a4 = new EndDevice(14, "A4");
        EndDevice a5 = new EndDevice(15, "A5");

        EndDevice b1 = new EndDevice(21, "B1");
        EndDevice b2 = new EndDevice(22, "B2");
        EndDevice b3 = new EndDevice(23, "B3");
        EndDevice b4 = new EndDevice(24, "B4");
        EndDevice b5 = new EndDevice(25, "B5");

        network.addDevice(hub1);
        network.addDevice(hub2);
        network.addDevice(core);
        network.addDevice(a1);
        network.addDevice(a2);
        network.addDevice(a3);
        network.addDevice(a4);
        network.addDevice(a5);
        network.addDevice(b1);
        network.addDevice(b2);
        network.addDevice(b3);
        network.addDevice(b4);
        network.addDevice(b5);

        network.connect("A1", "HUB-A");
        network.connect("A2", "HUB-A");
        network.connect("A3", "HUB-A");
        network.connect("A4", "HUB-A");
        network.connect("A5", "HUB-A");

        network.connect("B1", "HUB-B");
        network.connect("B2", "HUB-B");
        network.connect("B3", "HUB-B");
        network.connect("B4", "HUB-B");
        network.connect("B5", "HUB-B");

        network.connect("HUB-A", "CORE");
        network.connect("HUB-B", "CORE");

        network.printTopology();
        System.out.println("Broadcast domains: " + network.countBroadcastDomains());
        System.out.println("Collision domains: " + network.countCollisionDomains());

        a1.sendFrame("B4", "Hello across hubs via switch");
        core.printMacTable();
    }
}
