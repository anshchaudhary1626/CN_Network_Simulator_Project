package com.ansh.networksim.simulation;

import com.ansh.networksim.core.SimulatedHost;
import com.ansh.networksim.network.IPv4Address;
import com.ansh.networksim.network.RIPProtocol;
import com.ansh.networksim.network.Router;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates the complete predefined topology used by the full-stack demo.
 *
 * In simple words:
 * - This class is the "network setup" file.
 * - It creates hosts and routers.
 * - It assigns IP addresses and MAC addresses.
 * - It installs static routes.
 * - It runs RIP route exchange.
 *
 * Keeping this setup separate makes Main and FullStackSimulation cleaner.
 */
public class NetworkTopologyBuilder {
    public Topology build() {
        // The assignment requires a predefined topology, not a user-selected menu.
        System.out.println("[SIMULATION] Building predefined topology: HostA -- Switch1 -- Router1 -- Router2 -- Switch2 -- ServerB");

        // HostA is the client-side computer in network 192.168.1.0/24.
        SimulatedHost hostA = new SimulatedHost(
                "HostA",
                "00:AA:00:00:00:01",
                new IPv4Address("192.168.1.10/24"),
                "192.168.1.1"
        );
        // ServerB is the server-side computer in network 192.168.2.0/24.
        SimulatedHost serverB = new SimulatedHost(
                "ServerB",
                "00:BB:00:00:00:10",
                new IPv4Address("192.168.2.10/24"),
                "192.168.2.1"
        );

        // Router1 connects HostA's LAN to the router-to-router network.
        Router router1 = new Router("Router1");
        router1.addInterface("g0/0", new IPv4Address("192.168.1.1/24"), "02:00:00:00:01:01");
        router1.addInterface("s0/0", new IPv4Address("10.0.0.1/30"), "02:00:00:00:01:02");

        // Router2 connects the router-to-router network to ServerB's LAN.
        Router router2 = new Router("Router2");
        router2.addInterface("s0/0", new IPv4Address("10.0.0.2/30"), "02:00:00:00:02:01");
        router2.addInterface("g0/0", new IPv4Address("192.168.2.1/24"), "02:00:00:00:02:02");

        // Static routes are manually configured paths to the opposite LAN.
        router1.addStaticRoute("192.168.2.0/24", "10.0.0.2", "s0/0", 2);
        router2.addStaticRoute("192.168.1.0/24", "10.0.0.1", "s0/0", 2);

        // RIP then dynamically advertises connected networks between routers.
        RIPProtocol rip = new RIPProtocol();
        rip.exchangeRoutes(router1, router2, "10.0.0.1", "s0/0");
        rip.exchangeRoutes(router2, router1, "10.0.0.2", "s0/0");

        // Devices visible on HostA's local LAN for ARP resolution.
        Map<String, String> hostALan = new LinkedHashMap<>();
        hostALan.put(hostA.getIpOnly(), hostA.getMacAddress());
        hostALan.put("192.168.1.1", router1.getInterface("g0/0").getMacAddress());

        // Devices visible on ServerB's local LAN for ARP resolution.
        Map<String, String> serverBLan = new LinkedHashMap<>();
        serverBLan.put(serverB.getIpOnly(), serverB.getMacAddress());
        serverBLan.put("192.168.2.1", router2.getInterface("g0/0").getMacAddress());

        return new Topology(hostA, serverB, router1, router2, hostALan, serverBLan);
    }

    /**
     * Small container object that returns all devices and LAN maps together.
     *
     * A record is a short Java way to create a class that mainly stores data.
     */
    public record Topology(
            SimulatedHost hostA,
            SimulatedHost serverB,
            Router router1,
            Router router2,
            Map<String, String> hostALan,
            Map<String, String> serverBLan
    ) {
    }
}
