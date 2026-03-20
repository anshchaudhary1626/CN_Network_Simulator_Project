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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        runBasicPhysicalScenario();
        runHubScenario();
        runSwitchScenario();
        runCsmaCdScenario();
        runGoBackNScenario();
        runCombinedTopologyScenario();
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
