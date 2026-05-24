package com.ansh.networksim.transport;

import com.ansh.networksim.core.SimulatedHost;

/**
 * Represents one program/process running on a simulated host.
 *
 * Example:
 * - ServerB-DNS runs on ServerB at port 53.
 * - ServerB-HTTP runs on ServerB at port 80.
 *
 * This helps show process-to-process communication, not just host-to-host communication.
 */
public class Process {
    private final String processName;
    private final int portNumber;
    private final SimulatedHost host;

    public Process(String processName, int portNumber, SimulatedHost host) {
        // Connect the process name, port, and host together.
        this.processName = processName;
        this.portNumber = portNumber;
        this.host = host;
    }

    public String getProcessName() {
        return processName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public SimulatedHost getHost() {
        return host;
    }
}
