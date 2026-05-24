package com.ansh.networksim.network;

/**
 * Represents one router port/interface.
 *
 * Example:
 * - Router1 g0/0 connects to HostA LAN.
 * - Router1 s0/0 connects to Router2.
 *
 * Each interface needs:
 * - a name,
 * - an IP address,
 * - a MAC address.
 */
public class RouterInterface {
    private final String name;
    private final IPv4Address ipAddress;
    private final String macAddress;

    public RouterInterface(String name, IPv4Address ipAddress, String macAddress) {
        // Save the interface identity and addresses.
        this.name = name;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public IPv4Address getIpAddress() {
        return ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public String toString() {
        return name + "(" + ipAddress + ", " + macAddress + ")";
    }
}
