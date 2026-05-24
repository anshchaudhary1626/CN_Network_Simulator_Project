package com.ansh.networksim.core;

import com.ansh.networksim.network.ARPTable;
import com.ansh.networksim.network.IPv4Address;

/**
 * Represents one computer/end device in the simulated network.
 *
 * Example from our topology:
 * - HostA is a client computer.
 * - ServerB is a server computer.
 *
 * A real computer needs:
 * - a name, so humans can identify it,
 * - a MAC address for the data-link layer,
 * - an IP address for the network layer,
 * - an ARP table to remember IP -> MAC mappings,
 * - a default gateway IP when traffic must leave the local network.
 */
public class SimulatedHost {
    private final String name;
    private final String macAddress;
    private final IPv4Address ipAddress;
    private final ARPTable arpTable;
    private final String defaultGatewayIp;

    public SimulatedHost(String name, String macAddress, IPv4Address ipAddress, String defaultGatewayIp) {
        // Save all host identity details in one place so higher layers can use them.
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.defaultGatewayIp = defaultGatewayIp;
        // Each host has its own ARP cache, just like a real machine.
        this.arpTable = new ARPTable(name);
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public IPv4Address getIpAddress() {
        return ipAddress;
    }

    public String getIpOnly() {
        return ipAddress.getAddress();
    }

    public ARPTable getArpTable() {
        return arpTable;
    }

    public String getDefaultGatewayIp() {
        return defaultGatewayIp;
    }
}
