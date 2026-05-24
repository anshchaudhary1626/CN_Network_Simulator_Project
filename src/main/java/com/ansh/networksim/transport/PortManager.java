package com.ansh.networksim.transport;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Assigns port numbers to simulated processes.
 *
 * In simple words:
 * - IP address finds the computer.
 * - Port number finds the correct program/process on that computer.
 *
 * Well-known ports are fixed:
 * - DNS uses 53.
 * - HTTP uses 80.
 *
 * Client apps usually get temporary ephemeral ports.
 */
public class PortManager {
    public static final int DNS_PORT = 53;
    public static final int HTTP_PORT = 80;
    public static final int FIRST_EPHEMERAL_PORT = 49152;
    public static final int LAST_EPHEMERAL_PORT = 65535;

    private final Map<String, Integer> processPorts = new LinkedHashMap<>();
    private int nextEphemeralPort = FIRST_EPHEMERAL_PORT;

    public int assignWellKnownPort(String processName, int port) {
        // Only model the well-known ports required by the assignment.
        if (port != DNS_PORT && port != HTTP_PORT) {
            throw new IllegalArgumentException("Only DNS 53 and HTTP 80 are modelled as well-known ports");
        }
        processPorts.put(processName, port);
        System.out.println("[TRANSPORT] Assigned well-known port " + port + " to process " + processName);
        return port;
    }

    public int assignEphemeralPort(String processName) {
        // Give the next free temporary client port.
        if (nextEphemeralPort > LAST_EPHEMERAL_PORT) {
            throw new IllegalStateException("No ephemeral ports available");
        }
        int assigned = nextEphemeralPort++;
        processPorts.put(processName, assigned);
        System.out.println("[TRANSPORT] Assigned ephemeral port " + assigned + " to process " + processName);
        return assigned;
    }

    public int getPort(String processName) {
        // Look up a process that was already assigned a port.
        Integer port = processPorts.get(processName);
        if (port == null) {
            throw new IllegalArgumentException("No port assigned to " + processName);
        }
        return port;
    }
}
